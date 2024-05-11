package com.harvey.loadbalancer;

import java.util.List;

/**
 * @author Harvey Suen
 */
public interface LoadBalancer<T> {
    void add(List<T> itemList);
    
    T get();
}
