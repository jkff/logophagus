package org.lf.plugins.display;

import org.lf.plugins.Entity;

import javax.swing.*;

public interface DisplayPlugin {
    Class getInputType();

    JComponent createView(Entity entity);
}