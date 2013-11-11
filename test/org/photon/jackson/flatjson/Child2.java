package org.photon.jackson.flatjson;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Child2.class)
public class Child2 {

    private long id;
    private String name;
    @ManyToOne
    private Ref<Parent2> parentRef = new Ref<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Ref<Parent2> getParentRef() {
        return parentRef;
    }

    public void setParentRef(Ref<Parent2> parentRef) {
        this.parentRef = parentRef;
    }
}
