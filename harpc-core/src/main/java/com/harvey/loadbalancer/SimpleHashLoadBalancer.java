package com.harvey.loadbalancer;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.Random;

/**
 * @author Harvey Suen
 */
public class SimpleHashLoadBalancer<T> implements HashLoadBalancer<T> {
    private List<T> itemList;
    
    @Override
    public void add(List<T> itemList) {
        this.itemList = itemList;
    }
    
    @Override
    public T get() {
        return itemList.get(0);
    }
    
    @Override
    public T get(String itemKey) {
        if (CollUtil.isEmpty(itemList)) {
            return null;
        }
        
        int size = itemList.size();
        
        int idx = itemKey.hashCode() % size;
        
        return itemList.get(idx);
    }
}
