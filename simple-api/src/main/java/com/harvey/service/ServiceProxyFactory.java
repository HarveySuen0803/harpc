package com.harvey.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.harvey.model.RpcRequestMessage;
import com.harvey.model.RpcResponseMessage;
import com.harvey.serializer.Serializer;
import com.harvey.serializer.SerializerFactory;

import java.lang.reflect.Proxy;

/**
 * @author Harvey Suen
 */
public class ServiceProxyFactory {
    public static <T> T getService(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[]{clazz},
            (proxy, method, args) -> {
                Serializer serializer = SerializerFactory.getSerializer();

                RpcRequestMessage rpcReqMsg = new RpcRequestMessage(
                    clazz.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
                );

                byte[] rpcReqMsgBytes = serializer.serialize(rpcReqMsg);
                HttpResponse httpRep = HttpRequest.post("http://127.0.0.1:10100")
                    .body(rpcReqMsgBytes)
                    .execute();

                byte[] rpcRepMsgBytes = httpRep.bodyBytes();
                RpcResponseMessage rpcRepMsg = serializer.deserialize(rpcRepMsgBytes, RpcResponseMessage.class);

                return rpcRepMsg.getReturnValue();
            }
        );
    }
}
