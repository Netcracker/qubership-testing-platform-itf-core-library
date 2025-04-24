/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
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

import java.util.Objects;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.hazelcast.core.HazelcastInstance;

@Configuration
@Import(CommonHibernateConfiguration.class)
public class ExecutorHibernateConfiguration {

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
     * TODO: Add JavaDoc.
     */
    @Bean(name = "entityManagerFactory")
    public FactoryBean<EntityManagerFactory> getLocalContainerEntityManagerFactoryBean(
            DataSource dataSource, Properties jpaProperties) {
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
        emf.setJpaProperties(Objects.requireNonNull(jpaProperties));
        return emf;
    }

    /**
     * TODO: Add JavaDoc.
     */
    @Bean(name = "transactionManager")
    public JpaTransactionManager getJpaTransactionManager(FactoryBean<EntityManagerFactory> entityManagerFactory)
            throws Exception {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
