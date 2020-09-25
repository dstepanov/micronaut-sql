package io.micronaut.configuration.hibernate.jpa.proxy;

import io.micronaut.aop.MethodInterceptor;
import org.hibernate.proxy.HibernateProxy;

public interface IntroducedHibernateProxy extends HibernateProxy {

    void $registerInterceptor(MethodInterceptor<Object, Object> interceptor);

}
