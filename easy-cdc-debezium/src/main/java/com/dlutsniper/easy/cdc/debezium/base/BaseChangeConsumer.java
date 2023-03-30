package com.dlutsniper.easy.cdc.debezium.base;

import io.debezium.DebeziumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <pre>
 * Original Source Code:
 * https://github.com/debezium/debezium-server/blob/main/debezium-server-core/src/main/java/io/debezium/server/BaseChangeConsumer.java
 *
 * 1.jakarta
 *   fix javax.annotation.PostConstruct
 *   to  normal
 * 2.inject
 *   fix customStreamNameMapper
 *   to  normal
 * 3.getConfigSubset
 *   fix Config config
 *   to  Properties properties
 * </pre>
 */
public class BaseChangeConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseChangeConsumer.class);
    protected Properties debeziumProperties;
    protected StreamNameMapper streamNameMapper = (x) -> x;

    protected BaseChangeConsumer(Properties debeziumProperties, StreamNameMapper customStreamNameMapper) {
        this.debeziumProperties = debeziumProperties;
        if (customStreamNameMapper != null) {
            streamNameMapper = customStreamNameMapper;
        }
        LOGGER.info("Using '{}' stream name mapper", streamNameMapper);
    }

    protected Map<String, Object> getConfigSubset(Properties properties, String prefix) {
        final Map<String, Object> ret = new HashMap<>();
        Iterator<Object> it = properties.keys().asIterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith(prefix)) {
                final String newPropName = key.substring(prefix.length());
                ret.put(newPropName, properties.get(key));
            }
        }
        return ret;
    }

    protected byte[] getBytes(Object object) {
        if (object instanceof byte[]) {
            return (byte[]) object;
        } else if (object instanceof String) {
            return ((String) object).getBytes();
        }
        throw new DebeziumException(unsupportedTypeMessage(object));
    }

    protected String getString(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        throw new DebeziumException(unsupportedTypeMessage(object));
    }

    protected String unsupportedTypeMessage(Object object) {
        final String type = (object == null) ? "null" : object.getClass().getName();
        return "Unexpected data type '" + type + "'";
    }
}
