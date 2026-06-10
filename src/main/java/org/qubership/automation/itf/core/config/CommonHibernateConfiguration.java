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
     * Constructor of dataSource bean.
     * It's used in case atp.multi-tenancy.enabled=false.
     *
     * @param url           - Jdbc url to inner database,
     * @param username      - database username,
     * @param password      - database password,
     * @param driverClass   - driver class name,
     * @param maxPoolSize   - Maximum Connections Pool size,
     * @param minIdle       - Minimum number of idle connections to keep in the pool,
     * @param idleTimeOut   - Timeout to keep idle connection in the pool (milliseconds),
     * @param maxLifeTime   - Maximum lifetime of a connection in the pool (milliseconds),
     * @return DataSource object created and configured.
     */
    @Bean(name = "dataSource")
    @ConditionalOnProperty(value = "atp.multi-tenancy.enabled", havingValue = "false")
    public DataSource getDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClass,
            @Value("${spring.datasource.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.hikari.minimum-idle}") int minIdle,
            @Value("${spring.datasource.hikari.idle-timeout:180000}") int idleTimeOut,
            @Value("${spring.datasource.hikari.max-lifetime:0}") int maxLifeTime,
            @Value("${spring.datasource.hikari.keepalive-time:55000}") int keepaliveTime,
            @Value("${spring.datasource.hikari.connection-timeout:30000}") int connectionTimeout,
            @Value("${spring.datasource.url.tcpKeepAlive:false}") boolean tcpKeepAlive,
            @Value("${spring.datasource.url.socketTimeout:0}") int socketTimeout) {
        HikariConfig hikariConfig = new HikariConfig();

        // Mandatory configuration part
        hikariConfig.setDriverClassName(driverClass);

        // Add Jdbc-level keepalive parameters (if configured) to the JDBC URL
        String urlWithParams = url;
        if (tcpKeepAlive) {
            if (!url.contains("tcpKeepAlive")) {
                urlWithParams += (url.contains("?") ? "&" : "?") + "tcpKeepAlive=true";
            }
        }
        if (socketTimeout > 0) {
            if (!urlWithParams.contains("socketTimeout")) {
                urlWithParams += (urlWithParams.contains("?") ? "&" : "?") + "socketTimeout=" + socketTimeout;
            }
        }
        hikariConfig.setJdbcUrl(urlWithParams);

        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);

        // Custom configuration part

        // According to best practices, we shouldn't set max lifetime > 0
        // because it entails connection closing sudden for application thread using it.
        // Or, set it to very big ("infinite") value.
        hikariConfig.setMaxLifetime(maxLifeTime);

        // GitHub HikariCP documentation insists not to set connectionTestQuery,
        // if our driver supports JDBC4 Connection.isValid() API.
        // Currently we use org.postgresql:postgresql:42.3.9.
        // Since 42.0.0, driver supports JDBC 4.2.
        //hikariConfig.setConnectionTestQuery("SELECT 1");

        // After this timeout (milliseconds) an Idle connection is removed from the pool.
        // From HikariCP GitHub documentation:
        //  This setting only applies when minimumIdle is defined to be less than maximumPoolSize.
        //  Idle connections will not be retired once the pool reaches minimumIdle connections.
        //  Whether a connection is retired as idle or not is subject to a maximum variation
        //  of +30 seconds, and average variation of +15 seconds. A connection will never be retired
        //  as idle before this timeout. A value of 0 means that idle connections are never removed
        //  from the pool. The minimum allowed value is 10000ms (10 seconds).
        //  Default: 600000 (10 minutes).
        hikariConfig.setIdleTimeout(idleTimeOut);

        // An interval between pings (milliseconds), it determines how frequently HikariCP will attempt
        // to keep a connection alive, in order to prevent it from being timed out by the database
        // or network infrastructure.
        // It should be less than 'idleTimeOut' and of course less than 'maxLifeTime' (if set).
        // From HikariCP GitHub documentation:
        // - The minimum allowed value is 30000ms (30 seconds), but a value in the range of minutes
        //  is most desirable. Default: 120000 (2 minutes)
        hikariConfig.setKeepaliveTime(keepaliveTime);

        hikariConfig.setConnectionTimeout(connectionTimeout);

        return new HikariDataSource(hikariConfig);
    }

    /**
     * jpaProperties bean constructor.
     *
     * @return Properties object created and populated from configuration.
     */
    @Bean
    public Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.max_fetch_depth", "0");
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        properties.setProperty("hibernate.jdbc.batch_size", "10");
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
     * LockProvider bean constructor.
     * LockProvider bean is used to lock db operations when atp-itf-executor service pods are starting in parallel
     * (if two or more pods are starting at the same time).
     * It is necessary while updating of ITF environment statuses and upgrading of the history.
     * Also, this bean is used by JobRunner in atp-itf-reporting service for scheduled job which updates ITF contexts
     * statuses to STOPPED after some time (configured in properties).
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
