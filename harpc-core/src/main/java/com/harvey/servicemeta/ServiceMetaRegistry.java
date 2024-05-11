package com.harvey.servicemeta;

import java.util.List;

/**
 * @author Harvey Suen
 */
public interface ServiceMetaRegistry {
    void init(ServiceMetaRegistryConfig serviceMetaRegistryConfig);
    
    void heartbeat();
    
    void watch(String serviceNodeKey);
    
    void register(ServiceMeta serviceMeta);
    
    void unregister(ServiceMeta serviceMeta);
    
    List<ServiceMeta> getServiceMetaList(String serviceKey);
    
    void destroy();
}
