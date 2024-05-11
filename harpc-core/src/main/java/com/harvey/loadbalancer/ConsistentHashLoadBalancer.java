package com.harvey.loadbalancer;

import cn.hutool.core.collection.CollUtil;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Harvey Suen
 */
public class ConsistentHashLoadBalancer<T> implements HashLoadBalancer<T> {
    private final TreeMap<Integer, T> circleMap = new TreeMap<>();
    
    @Override
    public void add(List<T> itemList) {
        for (T item : itemList) {
            circleMap.put(item.hashCode(), item);
        }
    }
    
    @Override
    public T get() {
        return circleMap.firstEntry().getValue();
    }
    
    @Override
    public T get(String itemKey) {
        if (CollUtil.isEmpty(circleMap)) {
            return null;
        }
        
        Integer srcHash = itemKey.hashCode();
        
        Integer hitHash = circleMap.ceilingKey(srcHash);
        if (hitHash == null) {
            hitHash = circleMap.firstKey();
        }
        
        return circleMap.get(hitHash);
    }
}
