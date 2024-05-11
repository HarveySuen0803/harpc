package com.harvey.loadbalancer;

/**
 * @author Harvey Suen
 */
public interface HashLoadBalancer<T> extends LoadBalancer<T> {
    T get(String itemKey);
}
