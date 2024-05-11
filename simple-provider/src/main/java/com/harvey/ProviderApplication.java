package com.harvey;

import com.harvey.server.RpcServer;
import com.harvey.server.VertxRpcServer;
import com.harvey.service.HelloService;
import com.harvey.service.ServiceRegister;
import com.harvey.service.impl.HelloServiceImpl;

/**
 * @author Harvey Suen
 */
public class ProviderApplication {
    public static void main(String[] args) {
        // 注册 com.harvey.service.HelloService & HelloServiceImpl 到 Register Center 中
        ServiceRegister.addService(HelloService.class.getName(), new HelloServiceImpl());
        
        // 启动 Rpc Server, 对外提供 HelloService 的服务
        RpcServer rpcServer = new VertxRpcServer();
        rpcServer.doStart(10100);
    }
}
