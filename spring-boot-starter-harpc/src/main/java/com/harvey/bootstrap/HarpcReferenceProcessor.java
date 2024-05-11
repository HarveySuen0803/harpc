package com.harvey.bootstrap;

import com.harvey.service.client.RpcClient;
import com.harvey.service.client.RpcClientFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is a Spring BeanPostProcessor that handles the @HarpcReference annotation
 *
 * @author Harvey Suen
 */
public class HarpcReferenceProcessor implements BeanPostProcessor {
    private static final RpcClient RPC_CLIENT = RpcClientFactory.getRpcClient();
    
    /**
     * Cache the list of fields annotated with {@link HarpcReference} for each class.
     */
    private static final Map<Class<?>, List<Field>> FIELD_LIST_MAP = new ConcurrentHashMap<>();
    
    /**
     * Get the list of fields annotated with {@link HarpcReference} in the given class.
     */
    private List<Field> getAnnotatedFieldList(Class<?> beanClass) {
        return FIELD_LIST_MAP.computeIfAbsent(
            beanClass,
            cls -> Arrays.stream(cls.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(HarpcReference.class))
                .collect(Collectors.toList())
        );
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        List<Field> fieldList = getAnnotatedFieldList(beanClass);
        for (Field field : fieldList) {
            HarpcReference harpcReference = field.getAnnotation(HarpcReference.class);
            if (harpcReference == null) {
                continue;
            }
            
            Class<?> serviceClass = harpcReference.serviceClass();
            if (serviceClass == HarpcReference.DefaultServiceClass.class) {
                serviceClass = field.getType();
            }
            
            Object serviceImpl = RPC_CLIENT.getService(serviceClass);
            
            try {
                field.setAccessible(true);
                field.set(bean, serviceImpl);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return bean;
    }
}
