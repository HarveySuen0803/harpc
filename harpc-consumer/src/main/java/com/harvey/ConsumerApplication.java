package com.harvey;

import com.harvey.service.HelloService;
import com.harvey.service.client.RpcClient;
import com.harvey.service.client.RpcClientFactory;
import com.harvey.service.client.TcpRpcClient;

/**
 * @author Harvey Suen
 */
public class ConsumerApplication {
    public static void main(String[] args) {
        // 获取 HelloService 的 Proxy Service, 本质就是发送了一个 Rpc Request 给 Provider 的 Rpc Server
        RpcClient rpcClient = RpcClientFactory.getRpcClient();
        HelloService helloService = rpcClient.getService(HelloService.class);
        System.out.println(helloService.sayHello("harvey"));
    }
}
