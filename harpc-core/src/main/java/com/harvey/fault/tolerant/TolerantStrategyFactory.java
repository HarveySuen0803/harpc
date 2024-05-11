package com.harvey.fault.tolerant;

import com.harvey.common.SpiManager;

/**
 * @author Harvey Suen
 */
public class TolerantStrategyFactory {
    private static final TolerantStrategyConfig CONFIG = TolerantStrategyConfigFactory.getConfig();
    
    static {
        SpiManager.loadService(TolerantStrategy.class);
    }
    
    public static TolerantStrategy getTolerantStrategy() {
        return getTolerantStrategy(CONFIG.getTolerantStrategyType());
    }
    
    public static TolerantStrategy getTolerantStrategy(String tolerantStrategyType) {
        return SpiManager.getService(tolerantStrategyType, TolerantStrategy.class);
    }
}
