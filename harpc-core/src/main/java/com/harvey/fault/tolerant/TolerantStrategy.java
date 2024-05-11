package com.harvey.fault.tolerant;

import com.harvey.service.RpcResponseMessage;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Harvey Suen
 */
public interface TolerantStrategy {
    RpcResponseMessage doTolerant(Map<String, Object> ctx, Exception e) throws Exception;
}
