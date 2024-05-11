package com.harvey.servicemeta;

import com.harvey.common.ConfigUtil;

/**
 * @author Harvey Suen
 */
public class ServiceMetaRegistryConfigFactory {
    private static volatile ServiceMetaRegistryConfig config;
    
    private static final Object CONFIG_LOCK = new Object();
    
    private static final String CONFIG_PREFIX = "rpc.servicemeta.registry";
    
    public static ServiceMetaRegistryConfig getConfig() {
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
        config = ConfigUtil.loadConfig(ServiceMetaRegistryConfig.class, CONFIG_PREFIX);
    }
}
