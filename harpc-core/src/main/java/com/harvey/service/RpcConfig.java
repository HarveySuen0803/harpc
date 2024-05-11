package com.harvey.service;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class RpcConfig {
    private String serverHost = RpcConst.SERVER_HOST;
    
    private Integer serverPort = RpcConst.SERVER_PORT;
    
    private String serverVersion = RpcConst.SERVER_VERSION;
    
    private String serverType = RpcConst.SERVER_TYPE;
    
    private String clientType = RpcConst.CLIENT_TYPE;
    
    private boolean isMock = RpcConst.IS_MOCK;
}
