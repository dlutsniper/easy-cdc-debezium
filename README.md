# easy-cdc-debezium

## What is easy-cdc-debezium ?
easy-cdc-debezium is a simple CDC pattern component based on Spring / Spring Boot and Embedded Engine.

## Why use easy-cdc ?
Alibaba/canal is an optional CDC implementation, but it only supports MySQL.

Debezium is a distributed platform for CDC and supports lots of Database.
Most commonly, Debezium is deployed as an Apache Kafka Connect.
Kafka Connect provides excellent fault tolerance scalability and reliability.
Not every application needs this level of fault tolerance scalability and reliability,
Instead, some applications would prefer to embed Debezium connectors directly within the application space.
Debezium Server is a ready-to-use application that can use, but it is build on Quarkus framework.

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
easy-cdc is under the Apache 2.0 license. See the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) file for details.
