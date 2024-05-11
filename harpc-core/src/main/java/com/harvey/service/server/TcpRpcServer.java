package com.harvey.service.server;

import com.harvey.protocol.*;
import com.harvey.service.RpcRequestMessage;
import com.harvey.service.RpcResponseMessage;
import com.harvey.service.RpcServiceManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Harvey Suen
 */
public class TcpRpcServer implements RpcServer {
    private static final Logger log = LoggerFactory.getLogger(TcpRpcServer.class);
    
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();
        
        NetServer server = vertx.createNetServer();
        
        server.connectHandler(new connectHandler());
        
        server.listen(port, new ServerListenHandler(port));
    }
    
    /**
     * 处理 Connection Event, 给每一个 Event 单独创建 NetSocket
     */
    public static class connectHandler implements Handler<NetSocket> {
        @Override
        public void handle(NetSocket netSocket) {
            RequestMessageBufferHandler requestMessageBufferHandler = new RequestMessageBufferHandler(netSocket);
            RequestMessageBufferHandlerWrapper requestMessageBufferHandlerWrapper = new RequestMessageBufferHandlerWrapper(requestMessageBufferHandler);
            netSocket.handler(requestMessageBufferHandlerWrapper);
        }
    }
    
    /**
     * 包装 RequestMessageBufferHandler, 通过 RecordParser 解决 Packet Problem
     */
    public static class RequestMessageBufferHandlerWrapper implements Handler<Buffer> {
        // 通过 newFix(MessageConst.HEADER_LENGTH) 指定, 每次 RecordParser 处理单位数据大小就是 16B, 可以解决半包问题
        private final RecordParser recordParser = RecordParser.newFixed(MessageConst.HEADER_LENGTH);
        
        private final RequestMessageBufferHandler requestMessageBufferHandler;
        
        public RequestMessageBufferHandlerWrapper(RequestMessageBufferHandler requestMessageBufferHandler) {
            this.requestMessageBufferHandler = requestMessageBufferHandler;
            this.recordParser.handler(new PacketHandler());
        }
        
        @Override
        public void handle(Buffer buf) {
            recordParser.handle(buf);
        }
        
        private class PacketHandler implements Handler<Buffer> {
            private int bodyLength = -1;
            private Buffer fullBuf = Buffer.buffer();
            
            @Override
            public void handle(Buffer buf) {
                if (bodyLength == -1) {
                    // 处理 Header Buffer
                    fullBuf.appendBuffer(buf);
                    bodyLength = buf.getInt(MessageConst.HEADER_BODY_LENGTH_IDX);
                    recordParser.fixedSizeMode(bodyLength);
                } else {
                    // 处理 Body Buffer
                    fullBuf.appendBuffer(buf);
                    
                    // 处理 Full Buffer
                    requestMessageBufferHandler.handle(fullBuf);
                    
                    // 重制标记, 用于处理下一条消息
                    bodyLength = -1;
                    fullBuf = Buffer.buffer();
                    recordParser.fixedSizeMode(MessageConst.HEADER_LENGTH);
                }
            }
        }
    }
    
    /**
     * 处理 RpcRequestMessage, 调用对应的 Rpc Service, 返回 RpcResponseMessage, 响应调用结果
     */
    public static class RequestMessageBufferHandler implements Handler<Buffer> {
        private final NetSocket netSocket;
        
        public RequestMessageBufferHandler(NetSocket netSocket) {
            this.netSocket = netSocket;
        }
        
        @Override
        public void handle(Buffer reqMsgBuf) {
            Message<RpcResponseMessage> repMsg = new Message<>();
            Message<RpcRequestMessage> reqMsg = MessageDecoder.decode(reqMsgBuf);
            
            // 配置 Response Message 的 Header
            Message.Header repMsgHeader = new Message.Header();
            Message.Header reqMsgHeader = reqMsg.getHeader();
            repMsgHeader.setMagicNum(reqMsgHeader.getMagicNum());
            repMsgHeader.setVersion(reqMsgHeader.getVersion());
            repMsgHeader.setSerializerTypeKey(reqMsgHeader.getSerializerTypeKey());
            byte messageClazzKey = MessageClazzEnum.getKey(RpcResponseMessage.class);
            repMsgHeader.setMessageClazzKey(messageClazzKey);
            repMsgHeader.setStatus(reqMsgHeader.getStatus());
            repMsgHeader.setSequenceId(reqMsgHeader.getSequenceId());
            repMsg.setHeader(repMsgHeader);
            
            // 配置 Response Message 的 Body
            RpcResponseMessage rpcRepMsg = new RpcResponseMessage();
            RpcRequestMessage rpcReqMsg = reqMsg.getBody();
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
            } catch (Exception e) {
                e.printStackTrace();
                rpcRepMsg.setExceptionValue(new Exception(e.getCause().getMessage()));
            }
            repMsg.setBody(rpcRepMsg);
            
            Buffer repMsgBuf = MessageEncoder.encode(repMsg);
            
            netSocket.write(repMsgBuf);
        }
    }
    
    /**
     * 监听 Tcp Server
     */
    public static class ServerListenHandler implements Handler<AsyncResult<NetServer>> {
        private final int port;
        
        public ServerListenHandler(int port) {
            this.port = port;
        }
        
        @Override
        public void handle(AsyncResult<NetServer> res) {
            if (res.succeeded()) {
                log.info("Server is now listening on port {} ", port);
            } else {
                log.info("Failed to start server: {}", res.cause().getMessage());
            }
        }
    }
}
