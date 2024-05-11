package com.harvey.serializer;

import com.harvey.common.SpiManager;

/**
 * @author Harvey Suen
 */
public class SerializerFactory {
    private static final SerializerConfig CONFIG = SerializerConfigFactory.getConfig();
    
    static {
        SpiManager.loadService(Serializer.class);
    }
    
    public static Serializer getSerializer() {
        return getSerializer(CONFIG.getSerializerType());
    }
    
    public static Serializer getSerializer(String serializerType) {
        return SpiManager.getService(serializerType, Serializer.class);
    }
}
