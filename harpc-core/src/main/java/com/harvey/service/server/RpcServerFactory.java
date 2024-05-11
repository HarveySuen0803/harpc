package com.harvey.service.server;

import com.harvey.service.RpcConfig;
import com.harvey.service.RpcConfigFactory;

/**
 * @author Harvey Suen
 */
public class RpcServerFactory {
    private static final RpcConfig CONFIG = RpcConfigFactory.getConfig();
    
    public static RpcServer getRpcServer() {
        return getRpcServer(CONFIG.getServerType());
    }
    
    public static RpcServer getRpcServer(String serverType) {
        if (RpcServerConst.TCP.equals(serverType)) {
            return new TcpRpcServer();
        } else if (RpcServerConst.HTTP.equals(serverType)) {
            return new HttpRpcServer();
        }
        return new TcpRpcServer();
    }
}
