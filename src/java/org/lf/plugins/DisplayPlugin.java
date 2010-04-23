package org.lf.plugins;

import javax.swing.*;

public interface DisplayPlugin {
    Class getInputType();

    JComponent createView(Entity entity);
}