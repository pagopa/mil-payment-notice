/*
 * Trace.java
 *
 * 22 apr 2024
 */
package it.pagopa.swclient.mil.paymentnotice.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

/**
 * 
 * @author Antonio Tarricone
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR
})
@Inherited
public @interface Trace {

}
