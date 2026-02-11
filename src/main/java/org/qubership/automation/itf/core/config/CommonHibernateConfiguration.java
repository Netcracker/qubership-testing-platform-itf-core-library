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

import static org.qubership.automation.itf.core.CoreConstants.HIBERNATE_CACHE_HAZELCAST_INSTANCE_NAME;

import java.util.Properties;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;

@Configuration
public class CommonHibernateConfiguration {

    @Value("${hibernate.second.level.cache.enabled:false}")
    private boolean secondLevelCacheEnabled;

    @Value("${hibernate.query.cache.enabled:false}")
    private boolean queryCacheEnabled;

    @Value("${hibernate.show.sql:false}")
    private boolean showSql;

    @Value("${hibernate.format.sql:false}")
    private boolean formatSql;

    @Value("${hibernate.generate.statistics:false}")
    private boolean generateStatistics;

    @Value("${hibernate.cache.use.structured.entries:false}")
    private boolean useStructuredEntries;

    /**
     * TODO: Add JavaDoc.
     */
    @Bean(name = "dataSource")
    @ConditionalOnProperty(value = "atp.multi-tenancy.enabled", havingValue = "false")
    public DataSource getDataSource(@Value("${spring.datasource.url}") String url,
                                    @Value("${spring.datasource.username}") String username,
                                    @Value("${spring.datasource.password}") String password,
                                    @Value("${spring.datasource.driver-class-name}") String driverClass,
                                    @Value("${spring.datasource.hikari.maximum-pool-size}") int maxPoolSize,
                                    @Value("${spring.datasource.hikari.minimum-idle}") int minIdle,
                                    @Value("${spring.datasource.hikari.idle-timeout}") int idleTimeOut,
                                    @Value("${spring.datasource.hikari.max-lifetime}") int maxLifeTime) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setIdleTimeout(idleTimeOut);
        hikariConfig.setMaxLifetime(maxLifeTime);
        hikariConfig.setDriverClassName(driverClass);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * TODO: Add JavaDoc.
     */
    @Bean
    public Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.max_fetch_depth", "0");
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        properties.setProperty("hibernate.jdbc.batch_size", "10");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.globally_quoted_identifiers", "false");
        properties.setProperty("hibernate.connection.CharSet", "utf8");
        properties.setProperty("hibernate.connection.characterEncoding", "utf8");
        properties.setProperty("hibernate.connection.useUnicode", "true");
        properties.setProperty("hibernate.cache.use_second_level_cache", String.valueOf(secondLevelCacheEnabled));
        if (secondLevelCacheEnabled) {
            properties.setProperty("hibernate.cache.region.factory_class",
                    "com.hazelcast.hibernate.HazelcastCacheRegionFactory");
            properties.setProperty("hibernate.cache.use_query_cache", String.valueOf(queryCacheEnabled));
            properties.setProperty("hibernate.cache.hazelcast.instance_name",
                    HIBERNATE_CACHE_HAZELCAST_INSTANCE_NAME.stringValue());
        }
        properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
        properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));
        properties.setProperty("hibernate.generate_statistics", String.valueOf(generateStatistics));
        properties.setProperty("hibernate.cache.use_structured_entries", String.valueOf(useStructuredEntries));
        return properties;
    }

    /**
     * LockProvider bean uses to lock db operations when atp-itf-executor services starting in parallel (if stats two
     * or more pods at the same time). It is necessary while updating ITF environment statuses and upgrading history
     * Also this bean uses by JobRunner in atp-itf-reporting service for scheduled job which update ITF contexts
     * statuses to STOPPED after some time (configured in properties)
     */
    @Bean(name = "lockProvider")
    public LockProvider getLockProvider(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        JdbcTemplateLockProvider.Configuration configuration =
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .withTimeZone(TimeZone.getTimeZone("UTC"))
                        .build();
        return new JdbcTemplateLockProvider(configuration);
    }
}
