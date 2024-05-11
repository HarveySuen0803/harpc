package com.harvey;

import com.harvey.bootstrap.RpcProviderBootstrap;
import com.harvey.service.HelloService;
import com.harvey.service.RpcService;
import com.harvey.service.impl.HelloServiceImpl;
import com.harvey.servicemeta.ServiceMeta;

/**
 * @author Harvey Suen
 */
public class ProviderApplication {
    public static void main(String[] args) {
        RpcService<HelloService> rpcService = new RpcService<>();
        rpcService.setServiceName(HelloService.class.getName());
        rpcService.setService(new HelloServiceImpl());
        
        RpcProviderBootstrap.init(rpcService);
    }
}
