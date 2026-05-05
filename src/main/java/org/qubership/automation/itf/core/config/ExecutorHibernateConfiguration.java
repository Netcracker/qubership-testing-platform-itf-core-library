/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.qubership.automation.itf.core.config;

import java.net.URI;
import java.util.Objects;
import java.util.Properties;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import javax.sql.DataSource;

import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cache.HazelcastCachingProvider;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Import(CommonHibernateConfiguration.class)
public class ExecutorHibernateConfiguration {

    @Value("${hibernate.second.level.cache.enabled:false}")
    private boolean secondLevelCacheEnabled;

    private HazelcastInstance hazelcastInstance;

    @Autowired(required = false)
    public void setHazelcastInstance(@Qualifier("hazelcastCacheInstance") HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Bean
    public TxExecutor transactionExecutor(JpaTransactionManager transactionManager) {
        return new TxExecutor(transactionManager);
    }

    /**
     * Constructor of EntityManagerFactory in case hazelcastCacheInstance exists.
     * Ensures that hazelcastCacheInstance is initialized earlier.
     */
    @Bean(name = "entityManagerFactory")
    @ConditionalOnBean(name = "hazelcastCacheInstance")
    @DependsOn("hazelcastCacheInstance")
    public FactoryBean<EntityManagerFactory> getLocalContainerEntityManagerFactoryBean(
            DataSource dataSource, Properties jpaProperties) {
        return createEntityManagerFactory(dataSource, jpaProperties, hazelcastInstance);
    }

    /**
     * Constructor of EntityManagerFactory in case hazelcastCacheInstance doesn't exist.
     */
    @Bean(name = "entityManagerFactory")
    @ConditionalOnMissingBean(name = "hazelcastCacheInstance")
    public FactoryBean<EntityManagerFactory> getLocalContainerEntityManagerFactoryBeanWithoutHazelcast(
            DataSource dataSource,
            Properties jpaProperties) {
        return createEntityManagerFactory(dataSource, jpaProperties, null);
    }

    private FactoryBean<EntityManagerFactory> createEntityManagerFactory(
            DataSource dataSource,
            Properties jpaProperties,
            HazelcastInstance hazelcastInstance) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPackagesToScan("org.qubership.automation.itf.core.model.jpa");
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setMappingResources(
                "mapping/Environment.hbm.xml",
                "mapping/EventTrigger.hbm.xml",
                "mapping/Folder.hbm.xml",
                "mapping/Configuration.hbm.xml",
                "mapping/ParsingRule.hbm.xml",
                "mapping/Server.hbm.xml",
                "mapping/Step.hbm.xml",
                "mapping/StepContainer-CallChain-Situation.hbm.xml",
                "mapping/System.hbm.xml",
                "mapping/Operation.hbm.xml",
                "mapping/StubProject.hbm.xml",
                "mapping/Template.hbm.xml",
                "mapping/Interceptor.hbm.xml",
                "mapping/Counter.hbm.xml",
                "mapping/UpgradeHistory.hbm.xml",
                "mapping/EntitiesMigration.hbm.xml");

        if (hazelcastInstance != null && secondLevelCacheEnabled) {
            // 1. JCache provider configuring
            System.setProperty("hazelcast.jcache.provider.type", "member");
            CachingProvider provider = new HazelcastCachingProvider();

            // 2. Please use EXISTING HazelcastInstance by name
            Properties props = HazelcastCachingProvider.propertiesByInstanceName(hazelcastInstance.getName());
            props.setProperty("hazelcast.jcache.provider.type", "member");

            CacheManager manager = provider.getCacheManager(URI.create("hibernate-l2-cache"), null, props);

            // 3. Explicitly instruct Hibernate to use JCache and our CacheManager
            jpaProperties.put("hibernate.cache.region.factory_class",
                    "org.hibernate.cache.jcache.JCacheRegionFactory");
            jpaProperties.put("hibernate.javax.cache.uri", "hibernate-l2-cache");
            //jpaProperties.put("hibernate.javax.cache.cache_manager", manager);

            log.info("createEntityManagerFactory: hazelcastInstance '{}' is used", hazelcastInstance.getName());
        }

        emf.setJpaProperties(Objects.requireNonNull(jpaProperties));
        log.info("createEntityManagerFactory: jpaProperties: {}", jpaProperties);
        return emf;
    }

    /**
     * JPA Transaction Manager Bean.
     */
    @Bean(name = "transactionManager")
    @DependsOnDatabaseInitialization
    public JpaTransactionManager getJpaTransactionManager(FactoryBean<EntityManagerFactory> entityManagerFactory)
            throws Exception {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
