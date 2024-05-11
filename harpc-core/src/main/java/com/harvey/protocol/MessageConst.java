package com.harvey.protocol;

import cn.hutool.core.util.ObjUtil;
import com.harvey.service.RpcRequestMessage;
import com.harvey.service.RpcResponseMessage;
import lombok.Getter;

/**
 * @author Harvey Suen
 */
public class MessageConst {
    public static final int HEADER_LENGTH = 16;
    
    public static final int HEADER_BODY_LENGTH_IDX = 12;
    
    public static final byte[] MAGIC_NUM = new byte[]{'B', 'A', 'B', 'Y'};
    
    public static final byte VERSION = (byte) 1;
}
