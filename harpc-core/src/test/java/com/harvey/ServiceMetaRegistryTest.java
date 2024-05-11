package com.harvey;

import com.harvey.servicemeta.ServiceMetaRegistryConfig;
import com.harvey.servicemeta.ServiceMeta;
import com.harvey.servicemeta.EtcdServiceMetaRegistry;
import com.harvey.servicemeta.ServiceMetaRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Harvey Suen
 */
public class ServiceMetaRegistryTest {
    public final ServiceMetaRegistry serviceMetaRegistry = new EtcdServiceMetaRegistry();
    
    @BeforeEach
    public void initRegistry() {
        ServiceMetaRegistryConfig serviceMetaRegistryConfig = new ServiceMetaRegistryConfig();
        serviceMetaRegistry.init(serviceMetaRegistryConfig);
    }
    
    @Test
    public void register() {
        // key: /rpc/myService:1.0/127.0.0.1:10101
        // val: {"serviceName":"myService","serviceHost":"127.0.0.1","servicePort":"10101","serviceGroup":"default","serviceVersion":"1.0"}
        ServiceMeta serviceMeta1 = new ServiceMeta();
        serviceMeta1.setServiceName("myService");
        serviceMeta1.setServiceVersion("1.0");
        serviceMeta1.setServiceHost("127.0.0.1");
        serviceMeta1.setServicePort(10101);
        serviceMetaRegistry.register(serviceMeta1);
        
        ServiceMeta serviceMeta2 = new ServiceMeta();
        serviceMeta2.setServiceName("myService");
        serviceMeta2.setServiceVersion("1.0");
        serviceMeta2.setServiceHost("127.0.0.1");
        serviceMeta2.setServicePort(10102);
        serviceMetaRegistry.register(serviceMeta2);
        
        ServiceMeta serviceMeta3 = new ServiceMeta();
        serviceMeta3.setServiceName("myService");
        serviceMeta3.setServiceVersion("1.0");
        serviceMeta3.setServiceHost("127.0.0.1");
        serviceMeta3.setServicePort(10103);
        serviceMetaRegistry.register(serviceMeta3);
    }
    
    @Test
    public void unregister() {
        ServiceMeta serviceMeta1 = new ServiceMeta();
        serviceMeta1.setServiceName("myService");
        serviceMeta1.setServiceVersion("1.0");
        serviceMeta1.setServiceHost("127.0.0.1");
        serviceMeta1.setServicePort(10101);
        serviceMetaRegistry.unregister(serviceMeta1);
    }
    
    @Test
    public void getServiceMetaList() {
        String serviceName = "myService";
        String serviceVersion = "1.0";
        String serviceKey = String.format("%s:%s", serviceName, serviceVersion);
        List<ServiceMeta> serviceMetaList = serviceMetaRegistry.getServiceMetaList(serviceKey);
        System.out.println(serviceMetaList);
    }
    
    @Test
    public void heartbeat() {
        register();
        try { TimeUnit.SECONDS.sleep(300); } catch (Exception e) { e.printStackTrace(); }
    }
}
