package io.micronaut.configuration.hibernate.jpa.proxy;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.type.CompositeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public class InterceptedHibernateProxyFactory implements ProxyFactory, MethodInterceptor<Object, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptedHibernateProxyFactory.class);
    private static final Set<Class<?>> EXPECTED_INTERFACES = Collections.singleton(HibernateProxy.class);
    private static final String GET_HIBERNATE_LAZY_INITIALIZER = "getHibernateLazyInitializer";

    private final BeanContext beanContext;

    private String entityName;
    private Class<?> persistentClass;
    private CompositeType componentIdType;
    private Method getIdentifierMethod;
    private Method setIdentifierMethod;

    private LazyInitializer lazyInitializer;
    private BeanDefinition<?> beanDefinition;

    public InterceptedHibernateProxyFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public void postInstantiate(String entityName,
                                Class persistentClass,
                                Set<Class> interfaces,
                                Method getIdentifierMethod,
                                Method setIdentifierMethod,
                                CompositeType componentIdType) throws HibernateException {
        this.getIdentifierMethod = getIdentifierMethod;
        this.setIdentifierMethod = setIdentifierMethod;
        this.entityName = entityName;
        this.persistentClass = persistentClass;
        this.componentIdType = componentIdType;
        if (LOGGER.isWarnEnabled() && !EXPECTED_INTERFACES.equals(interfaces)) {
            LOGGER.warn("Expected a single set of 'org.hibernate.proxy.HibernateProxy.class' got {}", interfaces);
        }
    }

    @Override
    public HibernateProxy getProxy(Serializable id, SharedSessionContractImplementor session) throws HibernateException {
        if (beanDefinition == null) {
            beanDefinition = beanContext.findProxyTargetBeanDefinition(persistentClass, null)
                    .orElseThrow(() -> new HibernateException("Cannot find a proxy class, please annotate " + persistentClass + " with @IntroduceHibernateProxy."));
        }
        lazyInitializer = new InterceptedHibernateProxyLazyInitializer(entityName, persistentClass, id, session);
        Object proxyTargetBean = beanContext.getBean(beanDefinition);
        IntroducedHibernateProxy introducedHibernateProxy = (IntroducedHibernateProxy) proxyTargetBean;
        introducedHibernateProxy.$registerInterceptor(this);
        return introducedHibernateProxy;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String methodName = context.getMethodName();
        if ((Class<?>) context.getDeclaringType() == HibernateProxy.class) {
            if (GET_HIBERNATE_LAZY_INITIALIZER.equals(methodName)) {
                return lazyInitializer;
            }
            // writeReplace
            // TODO: implement writeReplace for uninitialized proxy
            return lazyInitializer.getImplementation();
        }

        // Handle identifier setter / getter

        Object[] parameterValues = context.getParameterValues();
        int params = parameterValues.length;
        if (params == 0 && getIdentifierMethod != null && methodName.equals(getIdentifierMethod.getName()) && lazyInitializer.isUninitialized()) {
            return lazyInitializer.getIdentifier();
        } else if (params == 1 && setIdentifierMethod != null & methodName.equals(setIdentifierMethod.getName())) {
            lazyInitializer.initialize();
            lazyInitializer.setIdentifier((Serializable) parameterValues[0]);
        }

        // Equals/hashcode should work as other Hibernate proxies:
        // methods present -> interceptor invoked to initialize proxy
        // methods missing -> trigger default Object implementation

        ExecutableMethod<Object, Object> executableMethod = context.getExecutableMethod();
        // It isn't possible to override target type for executableMethod
        ExecutableMethod proxyTargetMethod = beanDefinition.getRequiredMethod(executableMethod.getMethodName(), executableMethod.getArgumentTypes());
        if (componentIdType != null && componentIdType.isMethodOf(executableMethod.getTargetMethod())) {
            return proxyTargetMethod.invoke(lazyInitializer.getIdentifier(), parameterValues);
        }
        return proxyTargetMethod.invoke(lazyInitializer.getImplementation(), parameterValues);
    }
}
