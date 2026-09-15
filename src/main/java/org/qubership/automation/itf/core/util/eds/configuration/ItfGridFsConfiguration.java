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
     * The template this library's GridFS-backed storage reads and writes files through.
     *
     * @param mongoDatabaseFactory the database to store files in
     * @param mappingMongoConverter converts file metadata to and from its stored document form
     * @return a new {@link GridFsTemplate} over {@code mongoDatabaseFactory}
     */
    @Bean
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory mongoDatabaseFactory,
                                         MappingMongoConverter mappingMongoConverter) {
        GridFsTemplate gridFsTemplate = new GridFsTemplate(mongoDatabaseFactory, mappingMongoConverter);
        log.info("GridFsTemplate has been created!");
        return gridFsTemplate;
    }

    /**
     * Connects to the configured MongoDB instance (see {@link #getUri()}) and exposes the
     * configured {@link #database} through a {@link MongoDatabaseFactory}, with command and
     * connection-pool metrics reported to {@code meterRegistry}.
     *
     * @param meterRegistry where connection and command metrics are reported
     * @return a database factory backed by the configured MongoDB connection
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
     * Resolves {@code DBRef}s encountered while mapping stored documents, against
     * {@code mongoDatabaseFactory}'s database.
     *
     * @param mongoDatabaseFactory the database to resolve references against
     * @return a new {@link DefaultDbRefResolver} over {@code mongoDatabaseFactory}
     */
    @Bean
    public DbRefResolver dbRefResolver(MongoDatabaseFactory mongoDatabaseFactory) {
        return new DefaultDbRefResolver(mongoDatabaseFactory);
    }

    /**
     * The converter {@link #gridFsTemplate(MongoDatabaseFactory, MappingMongoConverter)} uses to map
     * file metadata to and from its stored document form, with the default simple-type handling and
     * {@code dbRefResolver} for any {@code DBRef} it encounters.
     *
     * @param dbRefResolver resolves {@code DBRef}s the converter encounters
     * @return a new {@link MappingMongoConverter} with default mapping context settings
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
