package com.jd.journalkeeper.coordinating.state.store.rocksdb;

import com.jd.journalkeeper.coordinating.state.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * RocksDBConfigParser
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/10
 */
public class RocksDBConfigParser {

    protected static final Logger logger = LoggerFactory.getLogger(RocksDBConfigParser.class);

    public static Options parse(Properties properties) {
        Options options = new Options();
        options.setCompressionType(CompressionType.LZ4_COMPRESSION)
                .setCompactionStyle(CompactionStyle.LEVEL);

        BlockBasedTableConfig tableOptions = new BlockBasedTableConfig();
        options.setTableFormatConfig(tableOptions);

        for (String key : properties.stringPropertyNames()) {
            String prefix = null;
            Object configInstance = null;

            if (key.startsWith(RocksDBConfigs.OPTIONS_PREFIX)) {
                prefix = RocksDBConfigs.OPTIONS_PREFIX;
                configInstance = options;
            } else if (key.startsWith(RocksDBConfigs.TABLE_OPTIONS_PREFIX)) {
                prefix = RocksDBConfigs.TABLE_OPTIONS_PREFIX;
                configInstance = tableOptions;
            } else {
                continue;
            }

            String fieldKey = key.substring(prefix.length(), key.length());
            String value = properties.getProperty(key);

            try {
                Method setterMethod = findSetterMethod(configInstance.getClass(), fieldKey);
                if (setterMethod == null) {
                    logger.warn("parse config error, method not found, key: {}, value: {}", key, value);
                    continue;
                }
                setterMethod.invoke(configInstance, PropertyUtils.convert(value, setterMethod.getParameters()[0].getType()));
            } catch (Exception e) {
                logger.error("parse config error, key: {}, value: {}", key, value, e);
            }
        }

        if (properties.containsKey(RocksDBConfigs.FILTER_BITSPER_KEY)) {
            tableOptions.setFilterPolicy(new BloomFilter(
                    PropertyUtils.convertInt(properties.getProperty(RocksDBConfigs.FILTER_BITSPER_KEY), 0)));
        }

        return options;
    }

    protected static Method findSetterMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("set" + StringUtils.capitalize(name))) {
                return method;
            }
        }
        return null;
    }
}