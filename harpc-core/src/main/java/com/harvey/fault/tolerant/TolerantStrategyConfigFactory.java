package com.harvey.fault.tolerant;

import com.harvey.common.ConfigUtil;

/**
 * @author Harvey Suen
 */
public class TolerantStrategyConfigFactory {
    private static volatile TolerantStrategyConfig config;
    
    private static final Object CONFIG_LOCK = new Object();
    
    private static final String CONFIG_PREFIX = "rpc.fault.tolerant";
    
    public static TolerantStrategyConfig getConfig() {
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
        config = ConfigUtil.loadConfig(TolerantStrategyConfig.class, CONFIG_PREFIX);
    }
}
