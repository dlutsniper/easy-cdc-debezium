# easy-cdc-debezium

## What is easy-cdc-debezium ?
easy-cdc-debezium是一个基于Spring / Spring Boot和嵌入式引擎的简单CDC模式组件。

## Why use easy-cdc-debezium ?
Alibaba/canal是一个可选的CDC实现，但它只支持MySQL。

Debezium是一个CDC的分布式平台，支持很多数据库。
最常见的是，Debezium被部署为Apache Kafka Connect。
Kafka Connect提供了出色的容错扩展性和可靠性。
不是每个应用程序都需要这种水平的容错可扩展性和可靠性、
相反，一些应用程序更愿意将Debezium连接器直接嵌入到应用空间中。
Debezium服务器是一个可以使用的即用型应用程序，但它是建立在Quarkus框架之上的。

## Getting started
- Add dependency
    - Maven:
      ```xml
      <dependency>
          <groupId>com.dlutsniper</groupId>
          <artifactId>easy-cdc-starter</artifactId>
          <version>Latest Version</version>
      </dependency>
      ```
    - Gradle
      ```groovy
      compile group: 'com.dlutsniper', name: 'easy-cdc-starter', version: 'Latest Version'
      ```
- Config File
    - application.yaml
      ```yaml
      spring:
        profiles:
          include: debezium
      ```
    - application-debezium.properties
      see the Debezium Server documentation:
      https://debezium.io/documentation/reference/2.1/operations/debezium-server.html

## License
easy-cdc-debezium is under the Apache 2.0 license. See the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) file for details.
