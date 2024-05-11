package com.harvey.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Harvey Suen
 */
public class RpcServiceManager {
    public static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    
    public static <T> T getService(String serviceName) {
        return (T) serviceMap.get(serviceName);
    }
    
    public static void addService(String serviceName, Object serviceImpl) {
        serviceMap.put(serviceName, serviceImpl);
    }
    
    public static void delService(String serviceName) {
        serviceMap.remove(serviceName);
    }
}