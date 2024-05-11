package com.harvey.loadbalancer;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class LoadBalancerConfig {
    private String loadBalancerType = LoadBalancerConst.RANDOM;
}
