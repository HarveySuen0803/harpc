package com.harvey.bootstrap;

import com.harvey.fault.retry.RetryStrategyConst;
import com.harvey.fault.tolerant.TolerantStrategyConst;
import com.harvey.loadbalancer.LoadBalancerConst;
import com.harvey.service.RpcConst;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Harvey Suen
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HarpcReference {
    Class<?> serviceClass() default DefaultServiceClass.class;
    
    String serviceVersion() default RpcConst.SERVER_VERSION;
    
    String loadBalancer() default LoadBalancerConst.RANDOM;
    
    String retryStrategy() default RetryStrategyConst.FIXED_INTERVAL_RETRY;
    
    String tolerantStrategy() default TolerantStrategyConst.FAIL_FAST;
    
    boolean isMock() default false;
    
    interface DefaultServiceClass {}
}
