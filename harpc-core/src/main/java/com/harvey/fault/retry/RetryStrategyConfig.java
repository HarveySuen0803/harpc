package com.harvey.fault.retry;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class RetryStrategyConfig {
    private String retryStrategyType = RetryStrategyConst.FIXED_INTERVAL_RETRY;
}
