package com.harvey.serializer;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author Harvey Suen
 */
public class JsonSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
        String json = gson.toJson(obj);
        return json.getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
        String json = new String(bytes, StandardCharsets.UTF_8);
        return gson.fromJson(json, clazz);
    }
    
    /**
     * 让 Gson 支持序列化 Class
     */
    public static class ClassCodec implements com.google.gson.JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }
}