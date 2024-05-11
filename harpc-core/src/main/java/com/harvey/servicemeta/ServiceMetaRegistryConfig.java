package com.harvey.servicemeta;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class ServiceMetaRegistryConfig {
    private String registryType = ServiceMetaRegistryConst.ETCD;
    
    private String serverHost = ServiceMetaRegistryConst.ETCD_SERVER_HOST;
    
    private Integer serverPort = ServiceMetaRegistryConst.ETCD_SERVER_PORT;
    
    private String username;
    
    private String password;
    
    private Long timeout = 10000L;
}
