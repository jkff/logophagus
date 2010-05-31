package org.lf.util;

import javax.swing.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class HierarchicalAction {
    private List<HierarchicalAction> children;
    private Action action;
    private String name;

    public HierarchicalAction(String name) {
        this.name = name;
    }

    public HierarchicalAction(Action action) {
        this.action = action;
    }

    public void addChild(HierarchicalAction child) {
        if (children == null) {
            children = newList();
        }
        children.add(child);
    }

    public HierarchicalAction[] getChildren() {
        HierarchicalAction[] empty = new HierarchicalAction[0];
        return children == null ? empty : children.toArray(empty);
    }

    public Action getAction() {
        return action;
    }

    public String getName() {
        return action == null ? name : (String) action.getValue(Action.NAME);
    }

}
