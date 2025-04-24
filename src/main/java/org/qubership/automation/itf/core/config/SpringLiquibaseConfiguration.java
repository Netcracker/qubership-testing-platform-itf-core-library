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

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@RequiredArgsConstructor
public class SpringLiquibaseConfiguration {

    private final ResourceLoader resourceLoader;

    @Bean
    @ConditionalOnMissingBean
    public LiquibaseProperties liquibaseProperties() {
        return new LiquibaseProperties();
    }

    /**
     * Liquibase config in case MultiTenancy is disabled.
     */
    @Bean(name = "springLiquibase")
    @ConditionalOnProperty(name = "atp.multi-tenancy.enabled", havingValue = "false", matchIfMissing = true)
    public SpringLiquibase springLiquibaseWithDisabledMultiTenancy(DataSource dataSource,
                                                                   LiquibaseProperties liquibaseProperties) {
        SpringLiquibase springLiquibase = new BeanAwareSpringLiquibase();
        springLiquibase.setResourceLoader(resourceLoader);
        springLiquibase.setChangeLog(liquibaseProperties.getChangeLog());
        springLiquibase.setContexts(liquibaseProperties.getContexts());
        springLiquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        springLiquibase.setDropFirst(liquibaseProperties.isDropFirst());
        springLiquibase.setShouldRun(liquibaseProperties.isEnabled());
        springLiquibase.setLabels(liquibaseProperties.getLabels());
        springLiquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        springLiquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        springLiquibase.setDataSource(dataSource);
        return springLiquibase;
    }

    /**
     * Liquibase config in case MultiTenancy is enabled.
     */
    @Bean(name = "springLiquibase")
    @ConditionalOnProperty(name = "atp.multi-tenancy.enabled", havingValue = "true")
    public SpringLiquibase springLiquibaseWithEnabledMultiTenancy(LiquibaseProperties liquibaseProperties) {
        SpringLiquibase springLiquibase = new BeanAwareSpringLiquibase();
        springLiquibase.setResourceLoader(resourceLoader);
        springLiquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        springLiquibase.setShouldRun(false);
        return springLiquibase;
    }
}
