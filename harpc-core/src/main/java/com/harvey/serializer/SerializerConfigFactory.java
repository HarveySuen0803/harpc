package com.harvey.serializer;

import com.harvey.common.ConfigUtil;

/**
 * @author Harvey Suen
 */
public class SerializerConfigFactory {
    private static volatile SerializerConfig config;
    
    private static final Object CONFIG_LOCK = new Object();
    
    private static final String CONFIG_PREFIX = "rpc.serializer";
    
    public static SerializerConfig getConfig() {
        if (config != null) {
            return config;
        }
        
        synchronized (CONFIG_LOCK) {
            if (config != null) {
                return config;
            }
            loadConfig();
        }
        
        return config;
    }
    
    private static void loadConfig() {
        config = ConfigUtil.loadConfig(SerializerConfig.class, CONFIG_PREFIX);
    }
}
