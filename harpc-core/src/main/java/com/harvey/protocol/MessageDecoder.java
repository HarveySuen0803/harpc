package com.harvey.protocol;

import cn.hutool.core.util.ArrayUtil;
import com.harvey.serializer.Serializer;
import com.harvey.serializer.SerializerFactory;
import com.harvey.serializer.SerializerTypeEnum;
import io.vertx.core.buffer.Buffer;

/**
 * @author Harvey Suen
 */
public class MessageDecoder {
    public static <T> Message<T> decode(Buffer buf) {
        Message.Header header = new Message.Header();
        
        byte[] magicNum = buf.getBytes(0, 4); // 4B BABY
        if (!ArrayUtil.equals(magicNum, MessageConst.MAGIC_NUM)) {
            throw new RuntimeException("The magic number of the message is illegal");
        }
        header.setMagicNum(magicNum);
        
        byte version = buf.getByte(4); // 1B 0x01 -> 1
        header.setVersion(version);
        
        byte serializerTypeKey = buf.getByte(5); // 1B 0x01 -> 1 -> jdk
        header.setSerializerTypeKey(serializerTypeKey);
        
        byte messageTypeKey = buf.getByte(6); // 1B 0x01 -> 1 -> REQUEST("success", 1)
        header.setMessageClazzKey(messageTypeKey);
        
        byte status = buf.getByte(7); // 1B 0x01 -> 1 -> SUCCESS(1)
        header.setStatus(status);
        
        int sequenceId = buf.getInt(8); // 4B
        header.setSequenceId(sequenceId);
        
        int bodyLength = buf.getInt(12); // 4B
        header.setBodyLength(bodyLength);
        
        // 读取指定长度的数据, 解决粘包问题
        byte[] bodyBytes = buf.getBytes(16, 16 + bodyLength);
        
        String serializerType = SerializerTypeEnum.getVal(serializerTypeKey);
        if (serializerType == null) {
            throw new RuntimeException("Serialization Protocol Error");
        }
        Serializer serializer = SerializerFactory.getSerializer(serializerType);
        
        Class<T> messageClazz = MessageClazzEnum.getVal(messageTypeKey);
        if (messageClazz == null) {
            throw new RuntimeException("Message type does not exist");
        }
        
        T body = serializer.deserialize(bodyBytes, messageClazz);
        
        Message<T> message = new Message<>();
        message.setHeader(header);
        message.setBody(body);
        
        return message;
    }
}
