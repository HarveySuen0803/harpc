package com.harvey.servicemeta;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Harvey Suen
 */
@Data
public class ServiceMeta {
    private String serviceName;
    
    private String serviceHost;
    
    private Integer servicePort;
    
    private String serviceGroup;
    
    private String serviceVersion;
    
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion); // myService:1.0
    }
    
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort); // myService:1.0/127.0.0.1:10101
    }
    
    public String getServiceAddress() {
        return String.format("%s:%s", serviceHost, servicePort);
    }
    
    public String getHttpServiceAddress() {
        if (StrUtil.contains(serviceHost, "http://")) {
            return String.format("%s:%s", serviceHost, servicePort);
        } else if (StrUtil.contains(serviceHost, "https://")) {
            return String.format("%s:%s", serviceHost, servicePort);
        }
        return String.format("http://%s:%s", serviceHost, servicePort);
    }
}
