package org.photon.jackson.flatjson;

import java.util.ArrayList;
import java.util.List;

public class Registry2 {

    private List<Parent2> parents;
    private List<Child2> children;

    public List<Parent2> getParents() {
        return parents;
    }

    public Registry2 addParent(Parent2 parent) {
        if (parents == null) {
            parents = new ArrayList<>();
        }
        parents.add(parent);
        return this;
    }

    public void setParents(List<Parent2> parents) {
        this.parents = parents;
    }

    public List<Child2> getChildren() {
        return children;
    }

    public Registry2 addChild(Child2 child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return this;
    }

    public void setChildren(List<Child2> children) {
        this.children = children;
    }
}
