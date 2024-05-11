package com.harvey.fault.tolerant;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class TolerantStrategyConfig {
    private String tolerantStrategyType = TolerantStrategyConst.FAIL_SAFE;
}
