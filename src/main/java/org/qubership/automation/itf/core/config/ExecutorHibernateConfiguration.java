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

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import javax.sql.DataSource;

import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.FactoryBean;
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

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
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

    @Bean
    public TxExecutor transactionExecutor(JpaTransactionManager transactionManager) {
        return new TxExecutor(transactionManager);
    }

    /**
     * Constructor of EntityManagerFactory in case hazelcastCacheInstance exists.
     * Ensures that hazelcastCacheInstance is initialized earlier.
     */
    @Bean(name = "entityManagerFactory")
    @ConditionalOnBean(name = "hazelcastClient") // Old: hazelcastCacheInstance
    @DependsOn("hazelcastClient")
    public FactoryBean<EntityManagerFactory> getLocalContainerEntityManagerFactoryBean(
            DataSource dataSource,
            Properties jpaProperties,
            @Qualifier("hazelcastClient") HazelcastInstance hazelcastInstance) {
        return createEntityManagerFactory(dataSource, jpaProperties, hazelcastInstance);
    }

    /**
     * Constructor of EntityManagerFactory in case hazelcastCacheInstance doesn't exist.
     */
    @Bean(name = "entityManagerFactory")
    @ConditionalOnMissingBean(name = "hazelcastClient")
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

        log.info("createEntityManagerFactory: secondLevelCacheEnabled {}", secondLevelCacheEnabled);
        if (hazelcastInstance != null && secondLevelCacheEnabled) {
            // 0. Add big caches to config...
            addBigCaches(hazelcastInstance.getConfig());

            // 1. JCache provider configuring
            System.setProperty("hazelcast.jcache.provider.type", "member");
            CachingProvider provider = new HazelcastCachingProvider();

            // 2. Use EXISTING HazelcastInstance by name
            Properties props = HazelcastCachingProvider.propertiesByInstanceName(hazelcastInstance.getName());
            props.setProperty("hazelcast.jcache.provider.type", "member");

            CacheManager manager = provider.getCacheManager(null, null, props);

            // 3. Explicitly instruct Hibernate to use JCache and our CacheManager
            jpaProperties.put("hibernate.cache.region.factory_class",
                    "org.hibernate.cache.jcache.JCacheRegionFactory");
            jpaProperties.put("hibernate.javax.cache.cache_manager", manager);

            log.info("createEntityManagerFactory: hazelcastInstance '{}' is used", hazelcastInstance.getName());
        } else {
            log.info("createEntityManagerFactory: hazelcastInstance is null or cache disabled");
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

    private void addBigCaches(Config config) {
        config.addCacheConfig(
                initBigRegionCache("projectsCache", 300, 12, TimeUnit.HOURS));

        config.addCacheConfig(
                initBigRegionCache("configurationsCache", 300000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("parsingRulesCache",80000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("keysToRegenerateCache",15000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("stepContainersCache",50000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(
                initBigRegionCache("eventTriggerCache",30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("conditionPropsCache",60000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("operationParsingRulesCache",70000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("systemParsingRulesCache",10000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(
                initBigRegionCache("templateCache",35000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("operationTemplateCache",30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("systemTemplateCache",5000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(
                initBigRegionCache("outboundTransportConfigurationCache",
                        30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("outboundTransportConfigurationsCollectionCache",
                        30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("outboundTemplateTransportConfigurationsCollectionCache",
                        70000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(initBigRegionCache("systemParsingRulesCollectionCache",
                7000, 60, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("operationParsingRulesCollectionCache",
                70000, 60, TimeUnit.MINUTES));

        config.addCacheConfig(initBigRegionCache("activeOperationEventTriggersCache",
                30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("operationByDefinitionKeyCache",
                20000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("operationSituationsCollectionCache",
                30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("systemTransportsCollectionCache",
                4000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("simpleSystemListByProjectCache",
                500, 240, TimeUnit.MINUTES));

        Map<String, CacheSimpleConfig> cacheConfigs = config.getCacheConfigs();
        log.info("CacheConfigs - {} entries", cacheConfigs.size());
        for (Map.Entry<String, CacheSimpleConfig> cfg : cacheConfigs.entrySet()) {
            log.info("CacheConfig: {}: \n   EvictionConfig {}\n   ExpiryPolicyFactoryConfig {}", cfg.getKey(),
                    cfg.getValue().getEvictionConfig(), cfg.getValue().getExpiryPolicyFactoryConfig());
        }
    }

    private CacheSimpleConfig initBigRegionCache(String cacheName,
                                                        int cacheSize,
                                                        int durationAmount,
                                                        TimeUnit durationTimeUnit) {
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT);
        evictionConfig.setSize(cacheSize);
        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
        cacheConfig.setName(cacheName);
        cacheConfig.setEvictionConfig(evictionConfig);

        CacheSimpleConfig.ExpiryPolicyFactoryConfig.DurationConfig durationConfig =
                new CacheSimpleConfig.ExpiryPolicyFactoryConfig.DurationConfig(durationAmount, durationTimeUnit);
        CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig timedExpiryConfig =
                new CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig(
                        CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig
                                .ExpiryPolicyType.ACCESSED,
                        durationConfig
                );
        CacheSimpleConfig.ExpiryPolicyFactoryConfig expiryConfig =
                new CacheSimpleConfig.ExpiryPolicyFactoryConfig(timedExpiryConfig);
        cacheConfig.setExpiryPolicyFactoryConfig(expiryConfig);
        return cacheConfig;
    }

}
