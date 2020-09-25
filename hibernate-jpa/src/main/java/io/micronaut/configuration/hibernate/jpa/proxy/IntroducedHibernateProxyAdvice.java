package io.micronaut.configuration.hibernate.jpa.proxy;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Prototype;

@Prototype
public class IntroducedHibernateProxyAdvice implements MethodInterceptor<Object, Object> {

    private final static String INITIALIZE_PROXY_METHOD = "$registerInterceptor";

    private MethodInterceptor<Object, Object> interceptor;

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (interceptor == null) {
            if (INITIALIZE_PROXY_METHOD.equals(context.getMethodName())) {
                interceptor = (MethodInterceptor<Object, Object>) context.getParameterValues()[0];
                return null;
            }
            return context.proceed();
        }
        return interceptor.intercept(context);
    }

}
