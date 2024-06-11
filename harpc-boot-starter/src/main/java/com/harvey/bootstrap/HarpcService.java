package com.harvey.bootstrap;

import com.harvey.service.RpcConst;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Harvey Suen
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HarpcService {
    Class<?> serviceClass() default DefaultServiceClass.class;
    
    String serviceVersion() default RpcConst.SERVER_VERSION;
    
    interface DefaultServiceClass {}
}
