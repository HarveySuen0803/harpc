package com.harvey.service;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class RpcService<T> {
    private String serviceName;
    
    private T service;
}
