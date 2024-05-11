package com.harvey.service.client;

/**
 * @author Harvey Suen
 */
public interface RpcClient {
    <T> T getService(Class<?> clazz);
    
    <T> T getMockService(Class<?> clazz);
}
