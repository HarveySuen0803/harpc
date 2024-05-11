package com.harvey.service.server;

import com.harvey.serializer.Serializer;
import com.harvey.serializer.SerializerFactory;
import com.harvey.service.RpcRequestMessage;
import com.harvey.service.RpcResponseMessage;
import com.harvey.service.RpcServiceManager;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Harvey Suen
 */
public class HttpRpcServer implements RpcServer {
    private static final Logger log = LoggerFactory.getLogger(HttpRpcServer.class);
    
    @Override
    public void doStart(int port) {
        // 设置 BlockedThreadCheck 的间隔为 1000s, 方便测试, 后续需要调整
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval(1000000L);
        
        Vertx vertx = Vertx.vertx(vertxOptions);
        
        HttpServer server = vertx.createHttpServer();
        
        server.requestHandler(new HttpServerRequestHandler());
        
        server.listen(port, (res) -> {
            if (res.succeeded()) {
                log.info("Server is now listening on port {} ", port);
            } else {
                log.info("Failed to start server: {}", res.cause().getMessage());
            }
        });
    }
    
    public static class HttpServerRequestHandler implements Handler<HttpServerRequest> {
        @Override
        public void handle(HttpServerRequest httpReq) {
            System.out.println("Received the http request: " + httpReq);
            
            httpReq.bodyHandler(new RpcRequestMessageBufferHandler(httpReq));
        }
    }
    
    private static class RpcRequestMessageBufferHandler implements Handler<Buffer> {
        private HttpServerRequest httpReq;
        
        public RpcRequestMessageBufferHandler(HttpServerRequest httpReq) {
            this.httpReq = httpReq;
        }
        
        @Override
        public void handle(Buffer rpcReqMsgBuf) {
            byte[] rpcReqMsgBytes = rpcReqMsgBuf.getBytes();
            Serializer serializer = SerializerFactory.getSerializer();
            RpcRequestMessage rpcReqMsg = serializer.deserialize(rpcReqMsgBytes, RpcRequestMessage.class);;
            
            RpcResponseMessage rpcRepMsg = new RpcResponseMessage();
            if (rpcReqMsg == null) {
                rpcRepMsg.setExceptionValue(new RuntimeException("The rpc request is null"));
                doResponse(httpReq, rpcRepMsg, serializer);
                return;
            }
            
            try {
                String serviceName = rpcReqMsg.getServiceName();
                String methodName = rpcReqMsg.getMethodName();
                Class<?>[] parameterTypes = rpcReqMsg.getParameterTypes();
                Object[] parameterValues = rpcReqMsg.getParameterValues();
                Object service = RpcServiceManager.getService(serviceName);
                Class<?> serviceClass = Class.forName(serviceName);
                if (service == null) {
                    service = serviceClass.getDeclaredConstructor().newInstance();
                }
                
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object res = method.invoke(service, parameterValues);
                rpcRepMsg.setReturnValue(res);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
                     InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                rpcRepMsg.setExceptionValue(new Exception(e.getCause().getMessage()));
            }
            
            doResponse(httpReq, rpcRepMsg, serializer);
        }
        
        private static void doResponse(HttpServerRequest httpReq, RpcResponseMessage rpcRepMsg, Serializer serializer) {
            byte[] rpcRepMsgBytes = serializer.serialize(rpcRepMsg);
            Buffer rpcRepMsgBuf = Buffer.buffer(rpcRepMsgBytes);
            HttpServerResponse httpRep = httpReq.response()
                .putHeader("Content-Type", "application/json");
            httpRep.end(rpcRepMsgBuf);
        }
    }
}
