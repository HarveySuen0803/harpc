package com.harvey.servicemeta;

import java.util.List;

/**
 * @author Harvey Suen
 */
public class ServiceMetaCache {
    private static List<ServiceMeta> serviceMetaList;
    
    public static void setServiceMetaList(List<ServiceMeta> serviceMetaList) {
        ServiceMetaCache.serviceMetaList = serviceMetaList;
    }
    
    public static List<ServiceMeta> getServiceMetaList() {
        return ServiceMetaCache.serviceMetaList;
    }
    
    public static void delServiceMetaList() {
        ServiceMetaCache.serviceMetaList = null;
    }
}
