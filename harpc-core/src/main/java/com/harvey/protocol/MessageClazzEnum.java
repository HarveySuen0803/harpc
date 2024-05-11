package com.harvey.protocol;

import cn.hutool.core.util.ObjUtil;
import com.harvey.service.RpcRequestMessage;
import com.harvey.service.RpcResponseMessage;

/**
 * @author Harvey Suen
 */
public enum MessageClazzEnum {
    RPC_REQUEST_MESSAGE((byte) 1, RpcRequestMessage.class),
    RPC_RESPONSE_MESSAGE((byte) 2, RpcResponseMessage.class);
    
    private final byte key;
    private final Class<?> val;
    
    MessageClazzEnum(byte key, Class<?> val) {
        this.key = key;
        this.val = val;
    }
    
    public static <T> Class<T> getVal(byte key) {
        MessageClazzEnum[] messageClazzEnums = MessageClazzEnum.values();
        for (MessageClazzEnum messageClazzEnum : messageClazzEnums) {
            if (ObjUtil.equals(messageClazzEnum.key, key)) {
                return (Class<T>) messageClazzEnum.val;
            }
        }
        return null;
    }
    
    public static <T> byte getKey(Class<T> val) {
        MessageClazzEnum[] messageClazzEnums = MessageClazzEnum.values();
        for (MessageClazzEnum messageClazzEnum : messageClazzEnums) {
            if (ObjUtil.equals(messageClazzEnum.val, val)) {
                return messageClazzEnum.key;
            }
        }
        throw new RuntimeException("Can not found the class: " + val);
    }
}
