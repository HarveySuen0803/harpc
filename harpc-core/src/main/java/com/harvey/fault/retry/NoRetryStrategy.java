package com.harvey.fault.retry;

import com.harvey.service.RpcResponseMessage;

import java.util.concurrent.Callable;

/**
 * @author Harvey Suen
 */
public class NoRetryStrategy implements RetryStrategy {
    @Override
    public RpcResponseMessage doRetry(Callable<RpcResponseMessage> task) throws Exception {
        return task.call();
    }
}
