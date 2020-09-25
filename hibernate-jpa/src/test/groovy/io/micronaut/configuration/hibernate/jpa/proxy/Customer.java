package io.micronaut.configuration.hibernate.jpa.proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Customer {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private DepartmentCompositeId departmentCompositeId;
    @ManyToOne(fetch = FetchType.LAZY)
    private DepartmentSimple departmentSimple;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DepartmentCompositeId getDepartmentCompositeId() {
        return departmentCompositeId;
    }

    public void setDepartmentCompositeId(DepartmentCompositeId departmentCompositeId) {
        this.departmentCompositeId = departmentCompositeId;
    }

    public DepartmentSimple getDepartmentSimple() {
        return departmentSimple;
    }

    public void setDepartmentSimple(DepartmentSimple departmentSimple) {
        this.departmentSimple = departmentSimple;
    }
}
