package com.harvey.serializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;

/**
 * @author Harvey Suen
 */
public class JsonSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        return JSON.toJSONString(obj).getBytes();
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        String jsonStr = new String(bytes);
        return JSON.parseObject(jsonStr, clazz, JSONReader.Feature.SupportClassForName);
    }
}