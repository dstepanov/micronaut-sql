package io.micronaut.configuration.hibernate.jpa.proxy;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanProperty;
import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.bytecode.enhance.spi.Enhancer;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.bytecode.spi.ReflectionOptimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IntrospectedHibernateBytecodeProvider implements BytecodeProvider {

    private final static Enhancer NO_OP = (className, originalBytes) -> null;

    @Override
    public ProxyFactoryFactory getProxyFactoryFactory() {
        return new InterceptedHibernateProxyFactoryFactory();
    }

    @Override
    public ReflectionOptimizer getReflectionOptimizer(Class clazz, String[] getterNames, String[] setterNames, Class[] types) {
        Optional<BeanIntrospection<?>> optionalBeanIntrospection = BeanIntrospector.SHARED.findIntrospection(clazz);
        if (!optionalBeanIntrospection.isPresent()) {
            return null;
        }
        BeanIntrospection<?> beanIntrospection = optionalBeanIntrospection.get();
        return new ReflectionOptimizer() {
            @Override
            public InstantiationOptimizer getInstantiationOptimizer() {
                return beanIntrospection::instantiate;
            }

            @Override
            public AccessOptimizer getAccessOptimizer() {
                List<BeanProperty> beanProperties = new ArrayList<>(beanIntrospection.getBeanProperties());
                return new AccessOptimizer() {

                    @Override
                    public String[] getPropertyNames() {
                        return beanProperties
                                .stream()
                                .map(BeanProperty::getName)
                                .toArray(String[]::new);
                    }

                    @Override
                    public Object[] getPropertyValues(Object object) {
                        return beanProperties
                                .stream()
                                .map(prop -> prop.get(object))
                                .toArray(Object[]::new);
                    }

                    @Override
                    public void setPropertyValues(Object object, Object[] values) {
                        for (int i = 0; i < beanProperties.size(); i++) {
                            beanProperties.get(i).set(object, values[i]);
                        }
                    }
                };
            }
        };
    }

    @Override
    public Enhancer getEnhancer(EnhancementContext enhancementContext) {
        return NO_OP;
    }

}
