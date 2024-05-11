package com.harvey.servicemeta;

import com.harvey.common.SpiManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Harvey Suen
 */
public class ServiceMetaRegistryFactory {
    private static final ServiceMetaRegistryConfig CONFIG = ServiceMetaRegistryConfigFactory.getConfig();
    
    // SpiManager 的 Map 是 Lv1 Cache, 这里的 Map 是 Lv2 Cache, 存储执行了 init() 之后的 ServiceMetaRegistry
    private static final Map<String, ServiceMetaRegistry> REGISTRY_MAP = new ConcurrentHashMap<>();
    
    static {
        SpiManager.loadService(ServiceMetaRegistry.class);
    }
    
    public static ServiceMetaRegistry getRegistry() {
        return getRegistry(CONFIG.getRegistryType());
    }
    
    public static ServiceMetaRegistry getRegistry(String registryType) {
        // 通过 ConcurrentHashMap 的 computeIfAbsent() 来获取 ServiceMetaRegistry, 既保证了效率, 也保证了线程安全
        return REGISTRY_MAP.computeIfAbsent(registryType, rt -> {
            ServiceMetaRegistry serviceMetaRegistry = SpiManager.getService(registryType, ServiceMetaRegistry.class);
            serviceMetaRegistry.init(CONFIG);
            Runtime.getRuntime().addShutdownHook(new Thread(serviceMetaRegistry::destroy));
            return serviceMetaRegistry;
        });
    }
}