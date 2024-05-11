package com.harvey.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Harvey Suen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequestMessage implements Serializable {
    private String serviceName;
    
    private String methodName;
    
    private Class<?> returnType;
    
    private Class<?>[] parameterTypes;
    
    private Object[] parameterValues;
}
