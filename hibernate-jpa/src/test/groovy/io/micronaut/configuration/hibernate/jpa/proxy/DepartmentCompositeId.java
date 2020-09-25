package io.micronaut.configuration.hibernate.jpa.proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.Set;

@Entity
@IntroduceHibernateProxy
public class DepartmentCompositeId implements Serializable {

    @Id
    private String a;
    @Id
    private String b;
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "departmentCompositeId")
    private Set<Customer> customers;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Set<Customer> customers) {
        this.customers = customers;
    }

}
