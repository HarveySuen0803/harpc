package com.harvey.loadbalancer;

import com.harvey.common.ConfigUtil;

/**
 * @author Harvey Suen
 */
public class LoadBalancerConfigFactory {
    private static volatile LoadBalancerConfig config;
    
    private static final Object CONFIG_LOCK = new Object();
    
    private static final String CONFIG_PREFIX = "rpc.loadbalancer";
    
    public static LoadBalancerConfig getConfig() {
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
        config = ConfigUtil.loadConfig(LoadBalancerConfig.class, CONFIG_PREFIX);
    }
}
