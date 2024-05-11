package com.harvey.serializer;

/**
 * @author Harvey Suen
 */
public class SerializerFactory {
    public static final Serializer jdkSerializer = new JdkSerializer();
    
    public static final Serializer jsonSerializer = new JsonSerializer();
    
    public static Serializer getSerializer() {
        return jdkSerializer;
    }
    
    public static Serializer getSerializer(int type) {
        if (type == 1) {
            return jdkSerializer;
        } else if (type == 2) {
            return jsonSerializer;
        } else {
            return jdkSerializer;
        }
    }
}
