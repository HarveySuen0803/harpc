package com.harvey.protocol;

import cn.hutool.core.util.ObjUtil;

/**
 * @author Harvey Suen
 */
public enum MessageStatusEnum {
    SUCCESS((byte) 10, "success"),
    FAILURE((byte) 20, "failure");
    
    private final byte key;
    private final String val;
    
    MessageStatusEnum(byte key, String val) {
        this.key = key;
        this.val = val;
    }
    
    public static String getVal(byte key) {
        MessageStatusEnum[] messageStatusEnums = MessageStatusEnum.values();
        for (MessageStatusEnum messageStatusEnum : messageStatusEnums) {
            if (ObjUtil.equals(messageStatusEnum.key, key)) {
                return messageStatusEnum.val;
            }
        }
        return null;
    }
    
    public static byte getKey(String val) {
        MessageStatusEnum[] messageStatusEnums = MessageStatusEnum.values();
        for (MessageStatusEnum messageStatusEnum : messageStatusEnums) {
            if (ObjUtil.equals(messageStatusEnum.val, val)) {
                return messageStatusEnum.key;
            }
        }
        throw new RuntimeException("Can not found the status, status: " + val);
    }
}
