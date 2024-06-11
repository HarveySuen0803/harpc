package com.harvey.bootstrap;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Harvey Suen
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({HarpcServiceProcessor.class, HarpcReferenceProcessor.class})
public @interface EnableHarpc {
}
