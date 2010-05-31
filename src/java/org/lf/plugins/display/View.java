package org.lf.plugins.display;

import javax.swing.*;

/**
 * Created on: 09.05.2010 14:26:53
 */
public interface View<S> {
    JComponent getComponent();

    S getState();
    void restoreState(S state);
}
