package org.photon.jackson.flatjson;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Parent.class)
public class Parent {

    private long id;
    private String name;
    @OneToMany
    private List<Child> children;

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

    public List<Child> getChildren() {
        return children;
    }

    public Parent addChild(Child child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        child.setParent(this);
        children.add(child);
        return this;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }
}
