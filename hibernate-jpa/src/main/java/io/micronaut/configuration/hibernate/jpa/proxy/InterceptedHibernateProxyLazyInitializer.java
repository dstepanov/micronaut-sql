package io.micronaut.configuration.hibernate.jpa.proxy;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.AbstractLazyInitializer;

import java.io.Serializable;

public class InterceptedHibernateProxyLazyInitializer extends AbstractLazyInitializer {

    protected final Class<?> persistentClass;

    protected InterceptedHibernateProxyLazyInitializer(String entityName,
                                                       Class<?> persistentClass,
                                                       Serializable id,
                                                       SharedSessionContractImplementor session) {
        super(entityName, id, session);
        this.persistentClass = persistentClass;
    }

    @Override
    public Class<?> getPersistentClass() {
        return persistentClass;
    }

}
