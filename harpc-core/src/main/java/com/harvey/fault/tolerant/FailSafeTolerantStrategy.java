package com.harvey.fault.tolerant;

import com.harvey.service.RpcResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Harvey Suen
 */
public class FailSafeTolerantStrategy implements TolerantStrategy {
    private static final Logger log = LoggerFactory.getLogger(FailSafeTolerantStrategy.class);
    
    @Override
    public RpcResponseMessage doTolerant(Map<String, Object> ctx, Exception e) throws Exception {
        log.info("Service failed, Exception: {}", e.getMessage());
        return new RpcResponseMessage();
    }
}
