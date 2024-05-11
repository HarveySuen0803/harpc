package com.harvey.handler;

import com.harvey.model.RpcRequestMessage;
import com.harvey.model.RpcResponseMessage;
import com.harvey.service.ServiceRegister;
import com.harvey.serializer.Serializer;
import com.harvey.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Harvey Suen
 */
public class HttpServerRequestHandler implements Handler<HttpServerRequest> {
    @Override
    public void handle(HttpServerRequest httpReq) {
        System.out.println("Received the http request: " + httpReq);
        
        HttpServerResponse httpRep = httpReq.response()
            .putHeader("Content-Type", "application/json");
        
        httpReq.bodyHandler((buf) -> {
            byte[] bytes = buf.getBytes();
            Serializer serializer = SerializerFactory.getSerializer();
            RpcRequestMessage rpcReqMsg = serializer.deserialize(bytes, RpcRequestMessage.class);
            RpcResponseMessage rpcRepMsg = new RpcResponseMessage();
            
            if (rpcReqMsg == null) {
                rpcRepMsg.setExceptionValue(new RuntimeException("The rpc request is null"));
                doResponse(httpRep, rpcRepMsg, serializer);
                return;
            }
            
            try {
                String serviceName = rpcReqMsg.getServiceName();
                String methodName = rpcReqMsg.getMethodName();
                Class<?>[] parameterTypes = rpcReqMsg.getParameterTypes();
                Object[] parameterValues = rpcReqMsg.getParameterValues();
                Object service = ServiceRegister.getService(serviceName);
                Class<?> serviceClass = Class.forName(serviceName);
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object res = method.invoke(service, parameterValues);
                rpcRepMsg.setReturnValue(res);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                rpcRepMsg.setExceptionValue(new Exception(e.getCause().getMessage()));
            }
            
            doResponse(httpRep, rpcRepMsg, serializer);
        });
    }
    
    private static void doResponse(HttpServerResponse httpRep, RpcResponseMessage rpcRepMsg, Serializer serializer) {
        byte[] bytes = serializer.serialize(rpcRepMsg);
        httpRep.end(Buffer.buffer(bytes));
    }
}
