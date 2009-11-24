package org.lf.plugins.interfaces;

import javax.swing.*;

public interface DisplayPlugin {
    Class getInputType();
    JComponent createView(Object data);
}