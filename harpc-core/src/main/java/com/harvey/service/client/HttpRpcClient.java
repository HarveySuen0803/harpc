package com.harvey.service.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.harvey.loadbalancer.LoadBalancer;
import com.harvey.loadbalancer.LoadBalancerConfig;
import com.harvey.loadbalancer.LoadBalancerConfigFactory;
import com.harvey.loadbalancer.LoadBalancerFactory;
import com.harvey.serializer.SerializerConfig;
import com.harvey.serializer.SerializerConfigFactory;
import com.harvey.service.RpcConfig;
import com.harvey.service.RpcConfigFactory;
import com.harvey.service.RpcRequestMessage;
import com.harvey.service.RpcResponseMessage;
import com.harvey.servicemeta.*;
import com.harvey.serializer.Serializer;
import com.harvey.serializer.SerializerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author Harvey Suen
 */
public class HttpRpcClient implements RpcClient {
    private static final RpcConfig rpcConfig = RpcConfigFactory.getConfig();
    
    private static final LoadBalancerConfig loadBalancerConfig = LoadBalancerConfigFactory.getConfig();
    
    private static final ServiceMetaRegistryConfig serviceMetaRegistryConfig = ServiceMetaRegistryConfigFactory.getConfig();
    
    private static final SerializerConfig serializerConfig = SerializerConfigFactory.getConfig();
    
    /**
     * 获取 Proxy Service, 代替发送了一个 Rpc Request 给 Provider 的 Rpc Server
     */
    @Override
    public <T> T getService(Class<?> clazz) {
        // 判断是否开启了 Mock
        RpcConfig rpcConfig = RpcConfigFactory.getConfig();
        if (rpcConfig.isMock()) {
            return getMockService(clazz);
        }
        
        // 发送 Rpc Request 给 Provider 的 Rpc Server, 并获取返回值
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ServiceInvocationHandler());
    }
    
    private static class ServiceInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 序列化 RpcRequestMessage 并发送给 Provider 的 Rpc Server
            String serializerType = serializerConfig.getSerializerType();
            Serializer serializer = SerializerFactory.getSerializer(serializerType);
            String serviceName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            Class<?> methodReturnType = method.getReturnType();
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            RpcRequestMessage rpcReqMsg = new RpcRequestMessage(serviceName, methodName, methodReturnType, methodParameterTypes, args);
            byte[] rpcReqMsgBytes = serializer.serialize(rpcReqMsg);
            
            // 从 Registry 中获取 ServiceMetaList, 然后通过 LoadBalancer 选择一个 ServiceMeta
            String serviceVersion = rpcConfig.getServerVersion();
            String serviceMetaRegistryType = serviceMetaRegistryConfig.getRegistryType();
            ServiceMetaRegistry serviceMetaRegistry = ServiceMetaRegistryFactory.getRegistry(serviceMetaRegistryType);
            String serviceKey = String.format("%s:%s", serviceName, serviceVersion);
            List<ServiceMeta> serviceMetaList = serviceMetaRegistry.getServiceMetaList(serviceKey);
            if (CollUtil.isEmpty(serviceMetaList)) {
                throw new RuntimeException("Cannot fount ServiceMetaList");
            }
            
            // 通过 LoadBalancer 获取 ServiceMeta
            String loadBalancerType = loadBalancerConfig.getLoadBalancerType();
            LoadBalancer<ServiceMeta> loadBalancer = LoadBalancerFactory.getService(loadBalancerType);
            loadBalancer.add(serviceMetaList);
            ServiceMeta serviceMeta = loadBalancer.get();
            String serviceAddress = serviceMeta.getHttpServiceAddress();
            
            // 同步堵塞等地返回结果
            HttpResponse httpRep = HttpRequest.post(serviceAddress).body(rpcReqMsgBytes).execute();
            
            // 反序列化 RpcResponseMessage
            byte[] rpcRepMsgBytes = httpRep.bodyBytes();
            RpcResponseMessage rpcRepMsg = serializer.deserialize(rpcRepMsgBytes, RpcResponseMessage.class);
            
            return rpcRepMsg.getReturnValue();
        }
    }
    
    /**
     * 模拟发送 Rpc Request 给 Provider 的 Rpc Server, 返回一个默认值, 用作测试
     */
    @Override
    public <T> T getMockService(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MockServiceInvocationHandler());
    }
    
    private static class MockServiceInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?> returnType = method.getReturnType();
            return getDefaultReturnValue(returnType);
        }
        
        private Object getDefaultReturnValue(Class<?> returnType) {
            if (returnType.isPrimitive()) {
                if (returnType == boolean.class) {
                    return false;
                } else if (returnType == short.class) {
                    return (short) 0;
                } else if (returnType == int.class) {
                    return 0;
                } else if (returnType == long.class) {
                    return 0L;
                } else if (returnType == double.class) {
                    return 0d;
                } else if (returnType == float.class) {
                    return 0f;
                }
            }
            return null;
        }
    }
}
