package com.harvey;

import com.harvey.loadbalancer.LoadBalancer;
import com.harvey.loadbalancer.LoadBalancerFactory;
import com.harvey.loadbalancer.RandomLoadBalancer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Harvey Suen
 */
public class LoadBalancerTest {
    @Test
    public void test01() {
        TreeMap<Integer, Object> circleMap = new TreeMap<>();
        
        Object obj1 = "hello obj1";
        System.out.println(obj1);
        Object obj2 = "hello obj2";
        System.out.println(obj2);
        Object obj3 = "hello obj3";
        System.out.println(obj3);
        
        circleMap.put(obj1.hashCode(), obj1);
        circleMap.put(obj2.hashCode(), obj2);
        circleMap.put(obj3.hashCode(), obj3);
        
        System.out.println(circleMap);
        
        Integer hash = "hello world".hashCode();
        
        Integer tarKey = circleMap.ceilingKey(hash);
        System.out.println(tarKey);
        if (tarKey == null) {
            tarKey = circleMap.firstKey();
        }
        System.out.println(tarKey);
        
        Object obj = circleMap.get(tarKey);
        System.out.println(obj);
    }
    
    @Test
    public void test02() {
        List<String> itemList = List.of("a", "b", "c", "d");
        
        LoadBalancer loadBalancer = LoadBalancerFactory.getService();
    }
}
