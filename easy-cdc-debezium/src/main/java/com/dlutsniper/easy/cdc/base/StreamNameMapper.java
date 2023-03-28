package com.dlutsniper.easy.cdc.base;

/**
 * @see io.debezium.server.BaseChangeConsumer
 * <pre>
 * Original Source Code:
 * https://github.com/debezium/debezium-server/blob/main/debezium-server-core/src/main/java/io/debezium/server/StreamNameMapper.java
 * </pre>
 */
public interface StreamNameMapper {
    String map(String topic);
}
