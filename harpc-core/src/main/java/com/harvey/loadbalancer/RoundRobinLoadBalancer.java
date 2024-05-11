package com.harvey.loadbalancer;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Harvey Suen
 */
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {
    private final AtomicInteger itemCount = new AtomicInteger(0);
    
    private List<T> itemList;
    
    @Override
    public void add(List<T> itemList) {
        this.itemList = itemList;
    }
    
    @Override
    public T get() {
        if (CollUtil.isEmpty(itemList)) {
            return null;
        }
        
        int size = itemList.size();
        
        int idx = itemCount.getAndIncrement() % size;
        
        return itemList.get(idx);
    }
}
