package com.harvey.fault.retry;

import com.harvey.common.SpiManager;

/**
 * @author Harvey Suen
 */
public class RetryStrategyFactory {
    private static final RetryStrategyConfig CONFIG = RetryStrategyConfigFactory.getConfig();
    
    static {
        SpiManager.loadService(RetryStrategy.class);
    }
    
    public static RetryStrategy getRetryStrategy() {
        return getRetryStrategy(CONFIG.getRetryStrategyType());
    }
    
    public static RetryStrategy getRetryStrategy(String retryType) {
        return SpiManager.getService(retryType, RetryStrategy.class);
    }
}
