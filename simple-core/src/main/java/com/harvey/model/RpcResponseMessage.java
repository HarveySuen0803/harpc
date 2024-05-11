package com.harvey.model;

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
public class RpcResponseMessage implements Serializable {
    private Object returnValue;
    
    private Exception exceptionValue;
}
