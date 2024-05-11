package com.harvey.loadbalancer;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.Random;

/**
 * @author Harvey Suen
 */
public class RandomLoadBalancer<T> implements LoadBalancer<T> {
    private final Random random = new Random();
    
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
        
        int idx = random.nextInt(size);
        
        return itemList.get(idx);
    }
}
