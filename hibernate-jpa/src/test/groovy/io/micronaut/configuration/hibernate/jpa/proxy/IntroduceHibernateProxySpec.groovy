/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.hibernate.jpa.proxy

import io.micronaut.aop.Introduced
import io.micronaut.context.ApplicationContext
import org.hibernate.proxy.HibernateProxy
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

class IntroduceHibernateProxySpec extends Specification {

    @Shared
    @AutoCleanup
    ApplicationContext applicationContext = ApplicationContext.run(
            'datasources.default.name': 'mydb',
            'jpa.default.properties.hibernate.hbm2ddl.auto': 'create-drop',
    )

    void "test customer with simple lazy"() {
        when:
            EntityManagerFactory entityManagerFactory = applicationContext.getBean(EntityManagerFactory)

        then:
            entityManagerFactory != null

        when:
            EntityManager em = entityManagerFactory.createEntityManager()
            def tx = em.getTransaction()
            tx.begin()
            def newDepartment = new DepartmentSimple(name: "Xyz")
            em.persist(newDepartment)
            def newCustomer = new Customer(name: "Joe")
            newCustomer.setDepartmentSimple(newDepartment)
            em.persist(newCustomer)
            em.flush()
            em.clear()

        then:
            def customer = em.find(Customer, newCustomer.getId())
            def department = customer.getDepartmentSimple()
            department instanceof Introduced
            department instanceof HibernateProxy
            department instanceof IntroducedHibernateProxy
            def lazyInitializer = ((HibernateProxy) department).getHibernateLazyInitializer()
            lazyInitializer.isUninitialized() == true
            department.getId()
            department.getId() == department.getId()
            department.hashCode()
            department.equals(department)
            lazyInitializer.isUninitialized() == true
            department.getName()
            department.getName() == department.getName()
            lazyInitializer.isUninitialized() == false

        cleanup:
            tx.rollback()
    }

    void "test customer with composite id"() {
        when:
            EntityManagerFactory entityManagerFactory = applicationContext.getBean(EntityManagerFactory)

        then:
            entityManagerFactory != null

        when:
            EntityManager em = entityManagerFactory.createEntityManager()
            def tx = em.getTransaction()
            tx.begin()
            def newDepartment = new DepartmentCompositeId(a: "xx", b: "yy", name: "Xyz")
            em.persist(newDepartment)
            def newCustomer = new Customer(name: "Joe")
            newCustomer.setDepartmentCompositeId(newDepartment)
            em.persist(newCustomer)
            em.flush()
            em.clear()

        then:
            def customer = em.find(Customer, newCustomer.getId())
            def department = customer.getDepartmentSimple()
            department instanceof Introduced
            department instanceof HibernateProxy
            department instanceof IntroducedHibernateProxy
            def lazyInitializer = ((HibernateProxy) department).getHibernateLazyInitializer()
            lazyInitializer.isUninitialized() == true
            department.getId()
            department.getId() == department.getId()
            department.hashCode()
            department.equals(department)
            lazyInitializer.isUninitialized() == true
            department.getName()
            department.getName() == department.getName()
            lazyInitializer.isUninitialized() == false

        cleanup:
            tx.rollback()
    }
}


