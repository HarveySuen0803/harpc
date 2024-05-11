package com.harvey.fault.tolerant;

import com.harvey.service.RpcResponseMessage;

import java.util.Map;

/**
 * @author Harvey Suen
 */
public class FailFastTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponseMessage doTolerant(Map<String, Object> ctx, Exception e) throws Exception {
        throw new RuntimeException("Service failed: " + e);
    }
}
