package org.photon.jackson.flatjson;

import java.util.ArrayList;
import java.util.List;

public class Registry {

    private List<Parent> parents;
    private List<Child> children;

    public List<Parent> getParents() {
        return parents;
    }

    public Registry addParent(Parent parent) {
        if (parents == null) {
            parents = new ArrayList<>();
        }
        parents.add(parent);
        return this;
    }

    public void setParents(List<Parent> parents) {
        this.parents = parents;
    }

    public List<Child> getChildren() {
        return children;
    }

    public Registry addChild(Child child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return this;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }
}
