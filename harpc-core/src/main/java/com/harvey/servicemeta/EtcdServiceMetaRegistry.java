package com.harvey.servicemeta;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author Harvey Suen
 */
@Slf4j
public class EtcdServiceMetaRegistry implements ServiceMetaRegistry {
    private Client etcdClient;
    
    private KV kvClient;
    
    private Lease leaseClient;
    
    private Watch watchClient;
    
    private static final String RPC_ROOT_PATH = "/rpc/";
    
    // 存储注册了 Rpc Service 的节点的 RpcServiceNodeKey, 用于 heartbeat()
    private Set<String> rpcServiceNodeKeySet = new HashSet<>();
    
    // 存储正在监听的节点的 RpcServiceNodeKey
    private Set<String> rpcServiceNodeKeyWatchingSet = new HashSet<>();
    
    @Override
    public void init(ServiceMetaRegistryConfig serviceMetaRegistryConfig) {
        String serverHost = serviceMetaRegistryConfig.getServerHost();
        Integer serverPort = serviceMetaRegistryConfig.getServerPort();
        String endpoints = String.format("http://%s:%s", serverHost, serverPort);
        
        etcdClient = Client.builder()
            .endpoints(endpoints)
            .build();
        
        kvClient = etcdClient.getKVClient();
        
        leaseClient = etcdClient.getLeaseClient();
        
        watchClient = etcdClient.getWatchClient();
        
        heartbeat();
    }
    
    @Override
    public void heartbeat() {
        // 每隔 10s 刷新一次 RpcServiceNodeKeySet 中所有的 RpcServiceNodeKey 的租约
        CronUtil.schedule("*/10 * * * * *", (Runnable) () -> {
            for (String rpcServiceNodeKey : rpcServiceNodeKeySet) {
                try {
                    ByteSequence key = ByteSequence.from(rpcServiceNodeKey, StandardCharsets.UTF_8);
                    List<KeyValue> kvList = kvClient.get(key).get().getKvs();
                    if (CollUtil.isEmpty(kvList)) {
                        continue;
                    }
                    
                    KeyValue kv = kvList.get(0);
                    ByteSequence val = kv.getValue();
                    String serviceMetaJson = val.toString(StandardCharsets.UTF_8);
                    ServiceMeta serviceMeta = JSONUtil.toBean(serviceMetaJson, ServiceMeta.class);
                    
                    register(serviceMeta);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
    
    @Override
    public void watch(String serviceNodeKey) {
        // 添加 ServiceNodeKey 到 WatchServiceNodeSet 中进行监听
        boolean isWatching = rpcServiceNodeKeyWatchingSet.add(serviceNodeKey);
        if (isWatching) {
            return;
        }
        
        // 如果 Key 触发了 Delete Event 就清除 ServiceMetaList Cache, 下次需要重新从 Registry 中获取 ServiceMetaList
        ByteSequence key = ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8);
        watchClient.watch(key, rep -> {
            for (WatchEvent event : rep.getEvents()) {
                WatchEvent.EventType eventType = event.getEventType();
                if (WatchEvent.EventType.DELETE == eventType) {
                    ServiceMetaCache.delServiceMetaList();
                }
            }
        });
    }
    
    @Override
    public void register(ServiceMeta serviceMeta) {
        try {
            // 创建 Key-Val
            String rpcServiceNodeKey = RPC_ROOT_PATH + serviceMeta.getServiceNodeKey(); // /rpc/myService:1.0/127.0.0.1:10101
            ByteSequence key = ByteSequence.from(rpcServiceNodeKey, StandardCharsets.UTF_8);
            String serviceMetaJson = JSONUtil.toJsonStr(serviceMeta);
            ByteSequence val = ByteSequence.from(serviceMetaJson, StandardCharsets.UTF_8);
            
            // 配置 Key-Val 的 Lease
            long leaseId = leaseClient.grant(30).get().getID();
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
            
            // 存储 Key-Val
            kvClient.put(key, val, putOption).get();
            
            // 向本地注册中心添加节点信息
            rpcServiceNodeKeySet.add(rpcServiceNodeKey);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void unregister(ServiceMeta serviceMeta) {
        try {
            // 删除 Key-Val
            String rpcServiceNodeKey = RPC_ROOT_PATH + serviceMeta.getServiceNodeKey();
            ByteSequence key = ByteSequence.from(rpcServiceNodeKey, StandardCharsets.UTF_8);
            kvClient.delete(key).get();
            
            // 从本地注册中心移除节点信息
            rpcServiceNodeKeySet.remove(rpcServiceNodeKey);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<ServiceMeta> getServiceMetaList(String serviceKey) {
        // 查询 ServiceMetaList Cache
        List<ServiceMeta> serviceMetaList = ServiceMetaCache.getServiceMetaList();
        if (CollUtil.isNotEmpty(serviceMetaList)) {
            return serviceMetaList;
        }
        
        try {
            // 查询 ServiceMetaList
            String rpcServiceKey = RPC_ROOT_PATH + serviceKey + "/"; // /rpc/myService:1.0/
            ByteSequence key = ByteSequence.from(rpcServiceKey, StandardCharsets.UTF_8);
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> kvList = kvClient.get(key, getOption).get().getKvs();
            serviceMetaList = kvList.stream()
                .map(kv -> {
                    // 监听 RpcServiceNodeKey
                    String rpcServiceNodeKey = kv.getKey().toString(StandardCharsets.UTF_8);
                    watch(rpcServiceNodeKey);
                    
                    // 获取 ServiceMeta
                    String serviceMetaJson = kv.getValue().toString(StandardCharsets.UTF_8);
                    return JSONUtil.toBean(serviceMetaJson, ServiceMeta.class);
                })
                .collect(Collectors.toList());
            
            // 存储 ServiceMetaList Cache
            ServiceMetaCache.setServiceMetaList(serviceMetaList);
            
            return serviceMetaList;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void destroy() {
        try {
            for (String rpcServiceNodeKey : rpcServiceNodeKeySet) {
                ByteSequence key = ByteSequence.from(rpcServiceNodeKey, StandardCharsets.UTF_8);
                kvClient.delete(key).get();
            }
            
            if (kvClient != null) {
                kvClient.close();
            }
            if (leaseClient != null) {
                leaseClient.close();
            }
            if (watchClient != null) {
                watchClient.close();
            }
            if (etcdClient != null) {
                etcdClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
