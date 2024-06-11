package com.harvey.bootstrap;

import com.harvey.service.RpcConfig;
import com.harvey.service.RpcConfigFactory;
import com.harvey.service.RpcServiceManager;
import com.harvey.service.server.RpcServer;
import com.harvey.service.server.RpcServerFactory;
import com.harvey.servicemeta.ServiceMeta;
import com.harvey.servicemeta.ServiceMetaRegistry;
import com.harvey.servicemeta.ServiceMetaRegistryFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is a Spring BeanPostProcessor that handles the @HarpcService annotation
 *
 * @author Harvey Suen
 */
public class HarpcServiceProcessor implements BeanPostProcessor {
    private static final RpcConfig RPC_CONFIG = RpcConfigFactory.getConfig();
    
    private static final AtomicBoolean IS_START = new AtomicBoolean(false);
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 判断是否为 HarpcService 标记的 Bean
        Class<?> beanClass = bean.getClass();
        HarpcService harpcService = beanClass.getAnnotation(HarpcService.class);
        if (harpcService == null) {
            return bean;
        }
        
        // 获取 Service
        Class<?> serviceClass = harpcService.serviceClass();
        if (serviceClass == HarpcService.DefaultServiceClass.class) {
            // Bean 为 ServiceImpl, Bean 的 Interface 为 Service
            Class<?>[] serviceClasses = beanClass.getInterfaces();
            if (serviceClasses.length == 0) {
                return bean;
            }
            serviceClass = serviceClasses[0];
        }
        String serviceName = serviceClass.getName();
        String serviceVersion = harpcService.serviceVersion();
        String serverHost = RPC_CONFIG.getServerHost();
        Integer serverPort = RPC_CONFIG.getServerPort();
        
        // 注册 Rpc Service 到 ServiceMap 中
        RpcServiceManager.addService(serviceName, bean);
        
        // 封装 Rpc Service 的元数据
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName(serviceName);
        serviceMeta.setServiceHost(serverHost);
        serviceMeta.setServicePort(serverPort);
        serviceMeta.setServiceVersion(serviceVersion);
        
        // 注册 Rpc Service 到 Registry 中, 既存储 Rpc Service 的元数据到 Registry 中
        ServiceMetaRegistry serviceMetaRegistry = ServiceMetaRegistryFactory.getRegistry();
        serviceMetaRegistry.register(serviceMeta);
        
        startRpcServer(serverPort);
        
        return bean;
    }
    
    private void startRpcServer(Integer serverPort) {
        if (IS_START.get()) {
            return;
        }
        
        if (!IS_START.compareAndSet(false, true)) {
            return;
        }
        
        RpcServer rpcServer = RpcServerFactory.getRpcServer();
        rpcServer.doStart(serverPort);
    }
}
