package com.harvey;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.harvey.service.RpcRequestMessage;
import org.junit.jupiter.api.Test;

/**
 * @author Harvey Suen
 */
public class SerializerTest {
    @Test
    public void serialize() throws ClassNotFoundException {
        RpcRequestMessage rpcReqMsg = new RpcRequestMessage();
        rpcReqMsg.setServiceName("com.harvey.service.HelloService");
        rpcReqMsg.setMethodName("sayHello");
        rpcReqMsg.setParameterTypes(new Class[]{String.class});
        rpcReqMsg.setParameterValues(new Object[]{"Harvey"});
        rpcReqMsg.setReturnType(String.class);
        
        String jsonStr = JSON.toJSONString(rpcReqMsg);
        System.out.println(jsonStr);
        
        RpcRequestMessage rpcRequestMessage = JSON.parseObject(jsonStr, RpcRequestMessage.class, JSONReader.Feature.SupportClassForName);
        System.out.println(rpcRequestMessage);
    }
}
