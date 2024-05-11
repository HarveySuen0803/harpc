package com.harvey.service.client;

import com.harvey.service.RpcConfig;
import com.harvey.service.RpcConfigFactory;
import com.harvey.service.server.HttpRpcServer;
import com.harvey.service.server.RpcServer;
import com.harvey.service.server.RpcServerConst;
import com.harvey.service.server.TcpRpcServer;

/**
 * @author Harvey Suen
 */
public class RpcClientFactory {
    private static final RpcConfig CONFIG = RpcConfigFactory.getConfig();
    
    public static RpcClient getRpcClient() {
        return getRpcClient(CONFIG.getClientType());
    }
    
    public static RpcClient getRpcClient(String clientType) {
        if (RpcClientConst.TCP.equals(clientType)) {
            return new TcpRpcClient();
        } else if (RpcServerConst.HTTP.equals(clientType)) {
            return new HttpRpcClient();
        }
        return new TcpRpcClient();
    }
}
