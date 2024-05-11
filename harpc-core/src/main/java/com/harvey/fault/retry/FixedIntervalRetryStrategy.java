package com.harvey.fault.retry;

import com.github.rholder.retry.*;
import com.harvey.service.RpcResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Harvey Suen
 */
public class FixedIntervalRetryStrategy implements RetryStrategy {
    private static final Logger log = LoggerFactory.getLogger(FixedIntervalRetryStrategy.class);
    
    @Override
    public RpcResponseMessage doRetry(Callable<RpcResponseMessage> task) throws Exception {
        Retryer<RpcResponseMessage> retryer = RetryerBuilder.<RpcResponseMessage>newBuilder()
            .retryIfExceptionOfType(Exception.class)
            .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    log.info("Retry times: {}", attempt.getAttemptNumber());
                }
            })
            .build();
        return retryer.call(task);
    }
}
