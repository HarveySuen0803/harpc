package com.harvey.service;

import com.harvey.common.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Harvey Suen
 */
@Slf4j
public class RpcConfigFactory {
    private static volatile RpcConfig config;
    
    private static final Object CONFIG_LOCK = new Object();
    
    private static final String CONFIG_PREFIX = "rpc";
    
    /**
     * 从 application.properties 中加载 RpcConfig
     */
    public static RpcConfig getConfig() {
        if (config != null) {
            return config;
        }
        
        synchronized (CONFIG_LOCK) {
            if (config != null) {
                return config;
            }
            loadRpcConfig();
        }
        
        return config;
    }
    
    private static void loadRpcConfig() {
        config = ConfigUtil.loadConfig(RpcConfig.class, CONFIG_PREFIX);
    }
}
