package com.harvey.fault.retry;

import com.harvey.service.RpcResponseMessage;

import java.util.concurrent.Callable;

/**
 * @author Harvey Suen
 */
public interface RetryStrategy {
    RpcResponseMessage doRetry(Callable<RpcResponseMessage> task) throws Exception;
}
