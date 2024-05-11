package com.harvey.fault.retry;

import com.harvey.common.ConfigUtil;

/**
 * @author Harvey Suen
 */
public class RetryStrategyConfigFactory {
    private static volatile RetryStrategyConfig config;
    
    private static final Object CONFIG_LOCK = new Object();
    
    private static final String CONFIG_PREFIX = "rpc.fault.retry";
    
    public static RetryStrategyConfig getConfig() {
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
        config = ConfigUtil.loadConfig(RetryStrategyConfig.class, CONFIG_PREFIX);
    }
}
