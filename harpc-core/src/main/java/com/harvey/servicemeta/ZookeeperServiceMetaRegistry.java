package com.harvey.servicemeta;

import cn.hutool.core.collection.CollUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Harvey Suen
 */
public class ZookeeperServiceMetaRegistry implements ServiceMetaRegistry {
    private CuratorFramework curatorClient;
    
    private ServiceDiscovery<ServiceMeta> serviceDiscovery;
    
    private RetryPolicy retryPolicy;
    
    private static final String RPC_ROOT_PATH = "/rpc/";
    
    private Set<String> rpcServiceNodeKeySet = new HashSet<>();
    
    private Set<String> rpcServiceNodeKeyWatchingSet = new HashSet<>();
    
    @Override
    public void init(ServiceMetaRegistryConfig serviceMetaRegistryConfig) {
        retryPolicy = new ExponentialBackoffRetry(3000, 10);
        
        curatorClient = CuratorFrameworkFactory.builder()
            .connectString("127.0.0.1:2181")
            .sessionTimeoutMs(60 * 1000)
            .connectionTimeoutMs(15 * 1000)
            .retryPolicy(retryPolicy)
            .build();
        
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
            .client(curatorClient)
            .basePath(RPC_ROOT_PATH)
            .serializer(new JsonInstanceSerializer<>(ServiceMeta.class))
            .build();
        
        try {
            curatorClient.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 存储的临时节点, 客户端断开连接后, 就会自动销毁, 不需要发送心跳来刷新过期时间, 肥肠安全
     */
    @Override
    public void heartbeat() {
    }
    
    @Override
    public void watch(String serviceNodeKey) {
        String rpcServiceNodeKey = String.format("%s/%s", RPC_ROOT_PATH, serviceNodeKey); // /rpc/my-service:1.0/127.0.0.1:10101
        boolean isWatching = rpcServiceNodeKeyWatchingSet.add(serviceNodeKey);
        if (isWatching) {
            return;
        }
        
        CuratorCache curatorCache = CuratorCache.builder(curatorClient, rpcServiceNodeKey).build();
        
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
            .forChanges((oldNode, newNode) -> ServiceMetaCache.delServiceMetaList())
            .forDeletes((oldNode) -> ServiceMetaCache.delServiceMetaList())
            .build();
        
        curatorCache.listenable().addListener(curatorCacheListener);
    }
    
    @Override
    public void register(ServiceMeta serviceMeta) {
        ServiceInstance serviceInstance = getServiceInstance(serviceMeta);
        try {
            serviceDiscovery.registerService(serviceInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        String rpcServiceNodeKey = RPC_ROOT_PATH + serviceMeta.getServiceNodeKey();
        rpcServiceNodeKeySet.add(rpcServiceNodeKey);
    }
    
    @Override
    public void unregister(ServiceMeta serviceMeta) {
        ServiceInstance serviceInstance = getServiceInstance(serviceMeta);
        try {
            serviceDiscovery.unregisterService(serviceInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        String rpcServiceNodeKey = RPC_ROOT_PATH + serviceMeta.getServiceNodeKey();
        rpcServiceNodeKeySet.remove(rpcServiceNodeKey);
    }
    
    @Override
    public List<ServiceMeta> getServiceMetaList(String serviceKey) {
        List<ServiceMeta> serviceMetaList = ServiceMetaCache.getServiceMetaList();
        if (CollUtil.isNotEmpty(serviceMetaList)) {
            return serviceMetaList;
        }
        
        try {
            Collection<ServiceInstance<ServiceMeta>> serviceInstanceColl = serviceDiscovery.queryForInstances(serviceKey);
            serviceMetaList = serviceInstanceColl.stream().map(ServiceInstance::getPayload).toList();
            ServiceMetaCache.setServiceMetaList(serviceMetaList);
            return serviceMetaList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void destroy() {
        try {
            for (String rpcServiceNodeKey : rpcServiceNodeKeySet) {
                curatorClient.delete().guaranteed().forPath(rpcServiceNodeKey);
            }
            
            if (serviceDiscovery != null) {
                serviceDiscovery.close();
            }
            if (curatorClient != null) {
                curatorClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private ServiceInstance getServiceInstance(ServiceMeta serviceMeta) {
        String serviceHost = serviceMeta.getServiceHost(); // 127.0.0.1
        Integer servicePort = serviceMeta.getServicePort(); // 10101
        String serviceAddress = serviceMeta.getServiceAddress(); // 127.0.0.1:10101
        String serviceKey = serviceMeta.getServiceKey(); // my-service:1.0
        
        try {
            return ServiceInstance.builder()
                .id(serviceAddress)
                .name(serviceKey)
                .address(serviceHost)
                .port(servicePort)
                .payload(serviceMeta)
                .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
