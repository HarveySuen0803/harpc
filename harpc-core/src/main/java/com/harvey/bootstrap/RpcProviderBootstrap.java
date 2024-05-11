package com.harvey.bootstrap;

import com.harvey.service.*;
import com.harvey.service.server.RpcServer;
import com.harvey.service.server.RpcServerFactory;
import com.harvey.servicemeta.ServiceMeta;
import com.harvey.servicemeta.ServiceMetaRegistry;
import com.harvey.servicemeta.ServiceMetaRegistryFactory;

import java.util.List;

/**
 * @author Harvey Suen
 */
public class RpcProviderBootstrap {
    private static final RpcConfig RPC_CONFIG = RpcConfigFactory.getConfig();
    
    public static void init(RpcService<?> rpcService) {
        List<RpcService<?>> rpcServiceList = List.of(rpcService);
        init(rpcServiceList);
    }
    
    public static void init(List<RpcService<?>> rpcServiceList) {
        // 获取 Registry
        ServiceMetaRegistry serviceMetaRegistry = ServiceMetaRegistryFactory.getRegistry();
        
        // 遍历 RpcServiceList 进行注册
        String serverHost = RPC_CONFIG.getServerHost();
        Integer serverPort = RPC_CONFIG.getServerPort();
        String serverVersion = RPC_CONFIG.getServerVersion();
        for (RpcService<?> rpcService : rpcServiceList) {
            // 获取 Rpc Service
            String serviceName = rpcService.getServiceName();
            Object service = rpcService.getService();
            
            // 注册 Rpc Service 到 ServiceMap 中
            RpcServiceManager.addService(serviceName, service);
            
            // 注册 Rpc Server 到 Registry 中
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceName(serviceName);
            serviceMeta.setServiceHost(serverHost);
            serviceMeta.setServicePort(serverPort);
            serviceMeta.setServiceVersion(serverVersion);
            serviceMetaRegistry.register(serviceMeta);
        }
        
        // 启动 Rpc Server, 对外提供服务
        RpcServer rpcServer = RpcServerFactory.getRpcServer();
        rpcServer.doStart(serverPort);
    }
}
