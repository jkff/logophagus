package org.lf.plugins.analysis;

import javax.swing.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class TreeAction {
    private List<TreeAction> children;
    private Action action;
    private String name;

    public TreeAction(String name) {
        this.name = name;
    }

    public TreeAction(Action action) {
        this.action = action;
    }

    public void addChild(TreeAction child) {
        if (children == null) {
            children = newList();
        }
        children.add(child);
    }

    public TreeAction[] getChildren() {
        TreeAction[] empty = new TreeAction[0];
        return children == null ? empty : children.toArray(empty);
    }

    public Action getAction() {
        return action;
    }

    public String getName() {
        return action == null ? name : (String) action.getValue(Action.NAME);
    }

}
