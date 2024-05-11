package com.harvey;

import com.harvey.common.SequenceIdUtil;
import com.harvey.protocol.Message;
import com.harvey.protocol.MessageConst;
import com.harvey.protocol.MessageDecoder;
import com.harvey.protocol.MessageEncoder;
import com.harvey.service.RpcRequestMessage;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

/**
 * @author Harvey Suen
 */
public class MessageTest {
    @Test
    public void encode() {
        Message<RpcRequestMessage> rpcMessage = new Message<>();
        
        Message.Header header = new Message.Header();
        header.setMagicNum(new byte[]{'B', 'A', 'B', 'Y'});
        header.setVersion((byte) 0x01); // 1
        header.setSerializerTypeKey((byte) 0x02); // json
        header.setMessageClazzKey((byte) 0x01); // RpcRequestMessage.class
        header.setStatus((byte) 0x01); // success
        header.setSequenceId(SequenceIdUtil.getSequenceId());
        rpcMessage.setHeader(header);
        
        RpcRequestMessage rpcRequestMessage = new RpcRequestMessage();
        rpcRequestMessage.setServiceName("myService");
        rpcRequestMessage.setMethodName("myMethod");
        rpcRequestMessage.setParameterTypes(new Class[]{String.class});
        rpcRequestMessage.setParameterValues(new Object[]{"param1"});
        rpcRequestMessage.setReturnType(String.class);
        rpcMessage.setBody(rpcRequestMessage);
        
        Buffer buf = MessageEncoder.encode(rpcMessage);
        System.out.println(buf);
        
        Message<?> message = MessageDecoder.decode(buf);
        System.out.println(message);
    }
}
