/*
 * TraceInterceptor.java
 *
 * 22 apr 2024
 */
package it.pagopa.swclient.mil.paymentnotice.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * 
 * @author Antonio Tarricone
 */
@Trace
@Interceptor
public class TraceInterceptor {
	/*
	 * 
	 */
	private Tracer tracer;

	/**
	 * 
	 * @param tracer
	 */
	@Inject
	TraceInterceptor(Tracer tracer) {
		this.tracer = tracer;
	}

	/**
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	Object trace(InvocationContext context) throws Exception {
		Span span = tracer.spanBuilder(context.getTarget().getClass().getSimpleName() + "." + context.getMethod().getName())
			.setParent(Context.current().with(Span.current()))
			.setSpanKind(SpanKind.INTERNAL)
			.startSpan();

		Object ret = context.proceed();

		span.end();

		return ret;
	}
}
