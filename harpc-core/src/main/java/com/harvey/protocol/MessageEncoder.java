package com.harvey.protocol;

import com.harvey.serializer.Serializer;
import com.harvey.serializer.SerializerFactory;
import com.harvey.serializer.SerializerTypeEnum;
import io.vertx.core.buffer.Buffer;

/**
 * @author Harvey Suen
 */
public class MessageEncoder {
    public static Buffer encode(Message<?> message) {
        if (message == null) {
            return Buffer.buffer();
        }
        
        Message.Header header = message.getHeader();
        if (header == null) {
            return Buffer.buffer();
        }
        
        Buffer buf = Buffer.buffer();
        
        buf.appendBytes(header.getMagicNum()); // 4B BABY
        
        buf.appendByte(header.getVersion()); // 1B 0x01 -> 1
        
        buf.appendByte(header.getSerializerTypeKey()); // 1B 0x01 -> 1 -> jdk
        
        buf.appendByte(header.getMessageClazzKey()); // 1B 0x01 -> 1 -> RPC_REQUEST_MESSAGE(1, RpcRequestMessage.class)
        
        buf.appendByte(header.getStatus()); // 1B 0x01 -> 1 -> SUCCESS(1)
        
        buf.appendInt(header.getSequenceId()); // 4B
        
        byte serializerTypeKey = header.getSerializerTypeKey();
        String serializerType = SerializerTypeEnum.getVal(serializerTypeKey);
        if (serializerType == null) {
            throw new RuntimeException("Serialization Protocol Error");
        }
        Serializer serializer = SerializerFactory.getSerializer(serializerType);
        
        Object body = message.getBody();
        byte[] bodyBytes = serializer.serialize(body);
        buf.appendInt(bodyBytes.length); // 4B
        buf.appendBytes(bodyBytes);
        
        return buf;
    }
}
