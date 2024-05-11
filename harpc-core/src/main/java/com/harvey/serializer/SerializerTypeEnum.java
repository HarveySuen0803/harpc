package com.harvey.serializer;

import cn.hutool.core.util.ObjUtil;
import lombok.val;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Harvey Suen
 */
public enum SerializerTypeEnum {
    JDK((byte) 1, "jdk"),
    JSON((byte) 2, "json"),
    HESSIAN((byte) 3, "hessian");
    
    private final byte key;
    private final String val;
    
    SerializerTypeEnum(byte key, String val) {
        this.key = key;
        this.val = val;
    }
    
    public static String getVal(byte key) {
        SerializerTypeEnum[] serializerTypeEnums = SerializerTypeEnum.values();
        for (SerializerTypeEnum serializerTypeEnum : serializerTypeEnums) {
            if (ObjUtil.equals(serializerTypeEnum.key, key)) {
                return serializerTypeEnum.val;
            }
        }
        return null;
    }
    
    public static byte getKey(String val) {
        SerializerTypeEnum[] serializerTypeEnums = SerializerTypeEnum.values();
        for (SerializerTypeEnum serializerTypeEnum : serializerTypeEnums) {
            if (ObjUtil.equals(serializerTypeEnum.val, val)) {
                return serializerTypeEnum.key;
            }
        }
        throw new RuntimeException("Can not found the serializer, serializer: " + val);
    }
}
