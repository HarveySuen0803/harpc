package com.harvey.common;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Harvey Suen
 */
@Slf4j
public class SpiManager {

    /**
     * 存储已加载的类, <ServiceClazz, <ServiceImplClazzKey, ServiceImplClazz>>
     */
    private static final Map<String, Map<String, Class<?>>> SERVICE_LOADER_MAP = new ConcurrentHashMap<>();

    /**
     * 存储对象实例缓存, 避免重复创建, <ServiceClazzName, ServiceImpl>
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义 SPI 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";
    
    /**
     * 扫描路径
     */
    private static final String[] SPI_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 加载该类型的 SPI
     */
    public static void loadService(Class<?> serviceClazz) {
        String serviceClazzName = serviceClazz.getName();
        Map<String, Class<?>> serviceImplClazzMap = new HashMap<>();
        for (String spiDir : SPI_DIRS) {
            List<URL> resourceList = ResourceUtil.getResources(spiDir + serviceClazzName);
            for (URL resource : resourceList) {
                try {
                    InputStreamReader isr = new InputStreamReader(resource.openStream());
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) { // jdk=com.harvey.serializer.JdkSerializer
                        String[] strArray = line.split("="); // [jdk, com.harvey.serializer.JdkSerializer]
                        if (strArray.length != 2) {
                            continue;
                        }
                        
                        String serviceImplClazzKey = strArray[0]; // jdk
                        Class<?> serviceImplClazz = Class.forName(strArray[1]); // JdkSerializer.class
                        serviceImplClazzMap.put(serviceImplClazzKey, serviceImplClazz);
                    }
                } catch (Exception e) {
                    log.error("Spi resource load error", e);
                }
            }
        }
        SERVICE_LOADER_MAP.put(serviceClazzName, serviceImplClazzMap); // { key: Serializer.class, val: [ { key: jdk, val: JdkSerializer.class }, { key: json, val: JsonSerializer.class } ] }
    }
    
    /**
     * 获取某个接口的实例
     */
    public static <T> T getService(String serviceImplClazzKey, Class<?> serviceClazz) { // jdk, Serializer.class
        // 获取该 ServiceClazz 的 ServiceImplClazzMap
        String serviceClazzName = serviceClazz.getName(); // Serializer.class
        Map<String, Class<?>> serviceImplClazzMap = SERVICE_LOADER_MAP.get(serviceClazzName); // [ { key: jdk, val: JdkSerializer.class }, { key: json, val: JsonSerializer.class } ] }
        if (serviceImplClazzMap == null) {
            throw new RuntimeException(String.format("SpiLoader has not loaded the class (%s)", serviceClazzName));
        }
        
        // 获取该 ServiceImplClazzMap 中 ServiceImplClazzKey 对应的 ServiceImplClazz
        Class<?> serviceImplClazz = serviceImplClazzMap.get(serviceImplClazzKey); // JdkSerializer.class
        if (serviceImplClazz == null) {
            throw new RuntimeException(String.format("The class (%s) loaded by SpiLoader does not have an implementation for this key (%s)", serviceClazzName, serviceImplClazzKey));
        }
        
        // 从 ServiceMap 中查询是否有该 Service
        String serviceImplClazzName = serviceImplClazz.getName();
        T serviceImpl = (T) SERVICE_MAP.get(serviceImplClazzName);
        if (serviceImpl == null) {
            try {
                serviceImpl = (T) serviceImplClazz.getDeclaredConstructor().newInstance();
                SERVICE_MAP.put(serviceImplClazzName, serviceImpl);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return serviceImpl;
    }
}