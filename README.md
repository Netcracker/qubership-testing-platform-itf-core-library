# Qubership Testing Platform ITF Core Library

- **Qubership Testing Platform ITF Core Library** (QSTP ITF CORE) is a core library for `Integrations Testing Framework` services
- ITF Core Library is used in the following services: 
  - itf-executor
  - itf-stubs
  - itf-reporting

## Functionality Description

ITF Core Library contains packages:
- org.qubership.automation.itf.core.model
  - The package implements the whole object model for ITF Services: configuration objects, execution/reporting objects and javers object changes history objects
- org.qubership.automation.itf.core.hibernate
  - Classes of Hibernate Object Managers, Spring Repositories, Types Converters
- org.qubership.automation.itf.core.config
  - Spring Beans Config Classes
- org.qubership.automation.itf.core.template.velocity.directives
  - Apache Velocity Directives developed by ITF and included in ITF-Executor and in ITF-Stubs Services in-the-box
- org.qubership.automation.itf.core.message.parser
  - Parse messages by means of so-called 'Parsing Rules' of various types
- org.qubership.automation.itf.core.stub.fast
  - Fast Stubs implementation classes
- org.qubership.automation.itf.core.util
  - Various Utility classes for above functionality and other applications.

## Local build

In IntelliJ IDEA, one can select 'github' Profile in Maven Settings menu on the right, then expand Lifecycle dropdown 
of atp-itf-core module, then select 'clean' and 'install' options and click 'Run Maven Build' green arrow button on the top.

Or, one can execute the command:
```bash
mvn -P github clean install
```

## Usage

### Connecting in Spring Boot application ###
#### 1. Add dependency into a service ####

```xml
<dependency>
    <groupId>org.qubership.atp</groupId>
    <artifactId>atp-itf-core</artifactId>
    <version>4.4.106-SNAPSHOT</version>
</dependency>
```
#### 2. Specify the required parameters in application.properties ####
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
