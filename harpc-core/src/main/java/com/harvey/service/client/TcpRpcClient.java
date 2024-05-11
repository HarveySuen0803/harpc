package com.harvey.service.client;

import com.harvey.common.SequenceIdUtil;
import com.harvey.fault.retry.RetryStrategy;
import com.harvey.fault.retry.RetryStrategyFactory;
import com.harvey.fault.tolerant.TolerantStrategy;
import com.harvey.fault.tolerant.TolerantStrategyFactory;
import com.harvey.loadbalancer.LoadBalancer;
import com.harvey.loadbalancer.LoadBalancerFactory;
import com.harvey.protocol.*;
import com.harvey.serializer.*;
import com.harvey.service.RpcConfig;
import com.harvey.service.RpcConfigFactory;
import com.harvey.service.RpcRequestMessage;
import com.harvey.service.RpcResponseMessage;
import com.harvey.servicemeta.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Harvey Suen
 */
public class TcpRpcClient implements RpcClient {
    private static final Logger log = LoggerFactory.getLogger(TcpRpcClient.class);
    
    private static final RpcConfig rpcConfig = RpcConfigFactory.getConfig();
    
    private static final SerializerConfig serializerConfig = SerializerConfigFactory.getConfig();
    
    @Override
    public <T> T getService(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new TcpRpcClient.ServiceInvocationHandler());
    }
    
    private static class ServiceInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 封装 Request Message 用于请求 Rpc Server
            Message<RpcRequestMessage> reqMsg = new Message<>();
            
            String serviceName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            Class<?> methodReturnType = method.getReturnType();
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            
            // 封装 Request Message 的 Body
            RpcRequestMessage rpcReqMsg = new RpcRequestMessage();
            rpcReqMsg.setServiceName(serviceName);
            rpcReqMsg.setMethodName(methodName);
            rpcReqMsg.setReturnType(methodReturnType);
            rpcReqMsg.setParameterTypes(methodParameterTypes);
            rpcReqMsg.setParameterValues(args);
            reqMsg.setBody(rpcReqMsg);
            
            // 封装 Request Message 的 Header
            Message.Header reqMsgHeader = new Message.Header();
            reqMsgHeader.setMagicNum(MessageConst.MAGIC_NUM);
            reqMsgHeader.setVersion(MessageConst.VERSION);
            String serializerType = serializerConfig.getSerializerType();
            byte serializerTypeKey = SerializerTypeEnum.getKey(serializerType);
            reqMsgHeader.setSerializerTypeKey(serializerTypeKey);
            byte messageClazzKey = MessageClazzEnum.getKey(RpcRequestMessage.class);
            reqMsgHeader.setMessageClazzKey(messageClazzKey);
            reqMsgHeader.setSequenceId(SequenceIdUtil.getSequenceId());
            reqMsg.setHeader(reqMsgHeader);
            
            // 从 ServiceMetaRegistry 中获取 ServiceMetaList
            String serviceVersion = rpcConfig.getServerVersion();
            ServiceMetaRegistry serviceMetaRegistry = ServiceMetaRegistryFactory.getRegistry();
            String serviceKey = String.format("%s:%s", serviceName, serviceVersion);
            List<ServiceMeta> serviceMetaList = serviceMetaRegistry.getServiceMetaList(serviceKey);
            // 通过 LoadBalancer 从 ServiceMetaList 中选择一个 ServiceMeta
            LoadBalancer<ServiceMeta> loadBalancer = LoadBalancerFactory.getService();
            loadBalancer.add(serviceMetaList);
            ServiceMeta serviceMeta = loadBalancer.get();
            
            RpcResponseMessage rpcRepMsg;
            try {
                // 通过 RetryStrategy 发送 Request Message 给对应 ServiceMeta 的 RpcServer
                RetryStrategy retryStrategy = RetryStrategyFactory.getRetryStrategy();
                rpcRepMsg = retryStrategy.doRetry(() -> doRequest(reqMsg, serviceMeta));
            } catch (Exception e) {
                // 通过 TolerantStrategy 处理 Exception
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getTolerantStrategy();
                rpcRepMsg = tolerantStrategy.doTolerant(null, e);
            }
            
            return rpcRepMsg.getReturnValue();
        }
    }
    
    private static RpcResponseMessage doRequest(Message<RpcRequestMessage> reqMsg, ServiceMeta serviceMeta) throws ExecutionException, InterruptedException {
        String serviceHost = serviceMeta.getServiceHost();
        Integer servicePort = serviceMeta.getServicePort();
        
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponseMessage> rpcRepMsgFuture = new CompletableFuture<>();
        netClient.connect(servicePort, serviceHost, res -> {
            if (res.failed()) {
                log.info("Failed to connect to the tcp server");
                return;
            }
            
            Buffer reqMsgBuf = MessageEncoder.encode(reqMsg);
            
            NetSocket socket = res.result();
            socket.write(reqMsgBuf);
            
            socket.handler(repMsgBuf -> {
                Message<RpcResponseMessage> repMsg = MessageDecoder.decode(repMsgBuf);
                RpcResponseMessage rpcRepMsg = repMsg.getBody();
                rpcRepMsgFuture.complete(rpcRepMsg);
            });
        });
        
        RpcResponseMessage rpcRepMsg = rpcRepMsgFuture.get();
        netClient.close();
        
        return rpcRepMsg;
    }
    
    @Override
    public <T> T getMockService(Class<?> clazz) {
        return null;
    }
}
