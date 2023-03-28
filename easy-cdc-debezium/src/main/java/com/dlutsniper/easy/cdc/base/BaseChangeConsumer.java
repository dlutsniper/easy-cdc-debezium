package com.dlutsniper.easy.cdc.base;

import io.debezium.DebeziumException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @see io.debezium.server.BaseChangeConsumer
 * <pre>
 * Original Source Code:
 * https://github.com/debezium/debezium-server/blob/main/debezium-server-core/src/main/java/io/debezium/server/BaseChangeConsumer.java
 *
 * 1.jakarta
 *   fix javax.annotation.PostConstruct
 *   to  jakarta.annotation.PostConstruct
 * 2.inject
 *   fix @javax.inject.Inject javax.enterprise.inject.Instance<T> customStreamNameMapper
 *   to  @Autowired(required = false) List<T> customStreamNameMapper
 *   If you need an extension, just declare a custom StreamNameMapper 's implementation
 * 3.getConfigSubset
 *   fix Map<String, Object> getConfigSubset(Config config, String prefix)
 *   to  Map<String, Object> getConfigSubset(Properties properties, String prefix)
 * </pre>
 */
public class BaseChangeConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseChangeConsumer.class);
    protected StreamNameMapper streamNameMapper = (x) -> x;
    @Autowired(required = false)
    private List<StreamNameMapper> customStreamNameMapper;

    @PostConstruct
    void init() {
        if (customStreamNameMapper != null && customStreamNameMapper.size() > 0) {
            streamNameMapper = customStreamNameMapper.get(0);
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
//    protected Map<String, String> convertHeaders(ChangeEvent<Object, Object> record) {
//        List<Header<Object>> headers = record.headers();
//        Map<String, String> result = new HashMap<>();
//        for (Header<Object> header : headers) {
//            result.put(header.getKey(), getString(header.getValue()));
//        }
//        return result;
//    }
}
