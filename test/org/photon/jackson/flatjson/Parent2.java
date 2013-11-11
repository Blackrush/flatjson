package org.photon.jackson.flatjson;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Parent2.class)
public class Parent2 {

    private long id;
    private String name;
    @OneToMany
    private List<Child2> children;

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

    public List<Child2> getChildren() {
        return children;
    }

    public Parent2 addChild(Child2 child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        child.getParentRef().set(this);
        children.add(child);
        return this;
    }

    public void setChildren(List<Child2> children) {
        this.children = children;
    }
}
