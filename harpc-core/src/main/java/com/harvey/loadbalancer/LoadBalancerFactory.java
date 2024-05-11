package com.harvey.loadbalancer;

import com.harvey.common.SpiManager;

/**
 * @author Harvey Suen
 */
public class LoadBalancerFactory {
    private static final LoadBalancerConfig CONFIG = LoadBalancerConfigFactory.getConfig();
    
    static {
        SpiManager.loadService(LoadBalancer.class);
    }
    
    public static LoadBalancer getService() {
        return getService(CONFIG.getLoadBalancerType());
    }
    
    public static LoadBalancer getService(String loadBalancerType) {
        return SpiManager.getService(loadBalancerType, LoadBalancer.class);
    }
}