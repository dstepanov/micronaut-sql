package io.micronaut.configuration.hibernate.jpa.proxy;

import io.micronaut.context.BeanContext;
import org.hibernate.HibernateException;
import org.hibernate.bytecode.spi.BasicProxyFactory;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.resource.beans.spi.ManagedBeanRegistry;

import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class InterceptedHibernateProxyFactoryFactory implements ProxyFactoryFactory {

    @Override
    public ProxyFactory buildProxyFactory(SessionFactoryImplementor sessionFactory) {
        BeanContext beanContext = sessionFactory.getServiceRegistry()
                .getService(ManagedBeanRegistry.class)
                .getBean(BeanContext.class)
                .getBeanInstance();
        return new InterceptedHibernateProxyFactory(beanContext);
    }

    @Override
    public BasicProxyFactory buildBasicProxyFactory(Class superClass, Class[] interfaces) {
        // Fallback?
        throw new HibernateException("IntroducedHibernateProxyFactoryFactory doesn't support to generate a BasicProxy for type " + superClass + " and interfaces " + Arrays.toString(interfaces) + ". Enable a different BytecodeProvider.");
    }

}
