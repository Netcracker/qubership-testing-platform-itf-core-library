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
  - See the [Velocity directives reference](docs/velocity-directives.md) for the full list, their parameters, and
    their error behavior
- org.qubership.automation.itf.core.message.parser
  - Parse messages by means of so-called 'Parsing Rules' of various types
- org.qubership.automation.itf.core.stub.fast
  - Fast Stubs implementation classes
- org.qubership.automation.itf.core.util
  - Various Utility classes for above functionality and other applications.

## Local build

In IntelliJ IDEA, one can select `github` Profile in Maven Settings menu on the right, then expand Lifecycle dropdown
of atp-itf-core module, then select 'clean' and 'install' options and click 'Run Maven Build' green arrow button on the top.

Or, one can execute the command:
```bash
mvn -P github clean install
```

## Usage

### Connecting in Spring Boot application
#### 1. Add dependency into a service

Two release lines are published, depending on the Spring Boot version the consuming service targets.

**Spring Boot 3.5.14 or later** (the current `main` branch): published to GitHub Packages, starting at `5.0.0`.

```xml
<dependency>
    <groupId>org.qubership.atp</groupId>
    <artifactId>atp-itf-core</artifactId>
    <version>5.0.1</version>
</dependency>
```

GitHub Packages requires authentication for every read, including a public repository. Add the repository:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Netcracker/qubership-testing-platform-itf-core-library</url>
    </repository>
</repositories>
```

and a matching server entry with a GitHub personal access token that has the `read:packages` scope:

```xml
<!-- ~/.m2/settings.xml -->
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

**Spring Boot versions before 3.5.14**: published to Maven Central, which Maven resolves against with no extra
repository configuration. `4.4.115` is the last release on this line.

```xml
<dependency>
    <groupId>org.qubership.atp</groupId>
    <artifactId>atp-itf-core</artifactId>
    <version>4.4.115</version>
</dependency>
```

See the [releases page](https://github.com/Netcracker/qubership-testing-platform-itf-core-library/releases) for the
latest version on either line.

#### 2. Specify the required parameters in application.properties
```properties
##======================DataBase configurations=======================
atp.multi-tenancy.enabled=${ATP_MULTI_TENANCY_ENABLED:false}
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=${SPRING_DATASOURCE_DRIVER_CLASS_NAME}

spring.datasource.hikari.minimum-idle=${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE}
spring.datasource.hikari.maximum-pool-size=${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE}
spring.datasource.hikari.idle-timeout=${SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT:180000}
spring.datasource.hikari.max-lifetime=${SPRING_DATASOURCE_HIKARI_MAX_LIFETIME:0}
spring.datasource.hikari.keepalive-time=${SPRING_DATASOURCE_HIKARI_KEEPALIVE_TIME:55000}
spring.datasource.hikari.connection-timeout=${SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT:25000}
spring.datasource.url.tcpKeepAlive=true
spring.datasource.url.socketTimeout=120000

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

##======================Feign clients (BV, Datasets) configurations=======================
feign.atp.bv.name=${FEIGN_ATP_BV_NAME}
feign.atp.bv.url=${FEIGN_ATP_BV_URL}
feign.atp.bv.route=${FEIGN_ATP_BV_ROUTE}
feign.atp.datasets.name=${FEIGN_ATP_DATASETS_NAME}
feign.atp.datasets.url=${FEIGN_ATP_DATASETS_URL}
feign.atp.datasets.route=${FEIGN_ATP_DATASETS_ROUTE}
```

`atp.multi-tenancy.enabled` has no default in this library. Leave it out of `application.properties` (not even the
`:false` fallback the other properties use) and the `dataSource` bean above is never created, because
`CommonHibernateConfiguration` declares it with `@ConditionalOnProperty(value = "atp.multi-tenancy.enabled",
havingValue = "false")` and no `matchIfMissing`. The context then fails at startup naming an unrelated bean instead
of the property:

```text
Error creating bean with name 'jdbcLockProvider' ... No qualifying bean of type 'javax.sql.DataSource' available
```

or, where Spring Boot's own `DataSourceAutoConfiguration` is also on the classpath, the context starts instead on
Spring Boot's own `HikariDataSource`, silently skipping the `tcpKeepAlive`, `socketTimeout` and `maxLifetime`
handling the `dataSource` bean above applies. Set the property to `false`, as shown above, for a single-tenant
deployment that uses this library's own `dataSource` bean. For any other value, including `true`, this library
creates no `DataSource` at all. Provide one another way, for example through the separate `atp-multitenancy` library
that multi-tenant deployments already depend on.

None of the six `feign.atp.*` properties has a default. They become required as soon as this library's
`org.qubership.automation.itf.core.util.feign` package is component-scanned, which autowires every Feign client it
declares. A context missing one of these properties fails at startup with the placeholder left unresolved in the
target URL rather than a message naming the property, for example:

```text
java.net.URISyntaxException: Illegal character in authority at index 8: http://${feign.atp.bv.url}
```
#### 3. Make sure the library's Spring beans are component-scanned

The library ships `@Component`/`@Service` beans (for example `CoreObjectManager`, `ApplicationConfig`) that your
application's own Spring context has to construct. Spring Boot's default component scan covers the package of your
`@SpringBootApplication` class and its sub-packages, so no extra step is needed when that package already contains
or sits above `org.qubership.automation.itf.core`. Otherwise, add the package explicitly:

```java
@SpringBootApplication(scanBasePackages = {"your.own.package", "org.qubership.automation.itf.core"})
```

Skipping this does not fail at startup: entry points such as `CoreObjectManager.getInstance()` and
`ApplicationConfig.getEnv()` throw `IllegalStateException` naming this requirement the first time your application
calls into the library.
