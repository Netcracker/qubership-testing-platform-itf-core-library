# ATP-ITF-CORE

**Qubership ATP-ITF-CORE** (QS-ATP-ITF-CORE) is a core library `Integrations Testing Framework`

## Core Library used in ITF services: 
* atp-itf-executor
* atp-itf-stubs
* atp-itf-reporting

## User Guide

#### Connecting in Spring Boot application: #####
##### 1. Add dependency: #####

```    
<dependency>
    <groupId>org.qubership.atp</groupId>
    <artifactId>atp-itf-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
##### 2. Specify the required parameters in application.properties #####
```properties
##======================DataBase configurations=======================
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=${SPRING_DATASOURCE_DRIVER_CLASS_NAME}

spring.datasource.hikari.minimum-idle=${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE}
spring.datasource.hikari.maximum-pool-size=${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE}
spring.datasource.hikari.idle-timeout=${SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT}
spring.datasource.hikari.max-lifetime=${SPRING_DATASOURCE_HIKARI_MAX_LIFETIME}

hibernate.second.level.cache.enabled=${HIBERNATE_SECOND_LEVEL_CACHE_ENABLED}

##======================Gridfs configurations=======================
eds.gridfs.enabled=${EDS_GRIDFS_ENABLED}
eds.gridfs.host=${MONGO_DB_ADDR}
eds.gridfs.port=${MONGO_DB_PORT}
eds.gridfs.database=${EDS_GRIDFS_DB}
eds.gridfs.username=${EDS_GRIDFS_USER}
eds.gridfs.password=${EDS_GRIDFS_PASSWORD}

##======================Hazelcast configurations=======================
hazelcast.cache.enabled=${HAZELCAST_CACHE_ENABLED}
hazelcast.client.name=${HAZELCAST_CLIENT_NAME}
hazelcast.cluster-name=${HAZELCAST_CLUSTER_NAME}
hazelcast.address=${HAZELCAST_ADDRESS}

##==================Integration with Spring Cloud======================
eureka.client.serviceUrl.defaultZone=${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```
