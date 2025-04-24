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

package org.qubership.automation.itf.core.util.eds.configuration;

import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsConnectionPoolListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "eds.gridfs.enabled", havingValue = "true")
public class ItfGridFsConfiguration {

    @Value("${eds.gridfs.host}")
    private String host;
    @Value("${eds.gridfs.port}")
    private String port;
    @Value("${eds.gridfs.database}")
    private String database;
    @Value("${eds.gridfs.username}")
    private String user;
    @Value("${eds.gridfs.password}")
    private String password;


    /**
     * TODO: Add JavaDoc.
     */
    @Bean
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory mongoDatabaseFactory,
                                         MappingMongoConverter mappingMongoConverter) {
        GridFsTemplate gridFsTemplate = new GridFsTemplate(mongoDatabaseFactory, mappingMongoConverter);
        log.info("GridFsTemplate has been created!");
        return gridFsTemplate;
    }

    /**
     * TODO: Add JavaDoc.
     */
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MeterRegistry meterRegistry) {
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .addCommandListener(new MongoMetricsCommandListener(meterRegistry))
                        .applyToConnectionPoolSettings((ConnectionPoolSettings.Builder builder) -> {
                            builder.addConnectionPoolListener(new MongoMetricsConnectionPoolListener(meterRegistry))
                                    .build();
                        })
                        .applyConnectionString(new ConnectionString(getUri()))
                        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                        .build());
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    /**
     * TODO: Add JavaDoc.
     */
    @Bean
    public DbRefResolver dbRefResolver(MongoDatabaseFactory mongoDatabaseFactory) {
        return new DefaultDbRefResolver(mongoDatabaseFactory);
    }

    /**
     * TODO: Add JavaDoc.
     */
    @Bean
    public MappingMongoConverter mappingMongoConverter(DbRefResolver dbRefResolver) {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT);
        mappingContext.afterPropertiesSet();
        return new MappingMongoConverter(dbRefResolver, mappingContext);
    }

    private String getUri() {
        StringBuilder sb = new StringBuilder();
        sb.append("mongodb://")
                .append(user).append(":").append(password).append("@")
                .append(host).append(":").append(Integer.parseInt(port))
                .append("/?authSource=").append(database);
        return sb.toString();
    }
}
