package org.lf.ui.components.plugins.scrollablelog.extension;

import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

import javax.swing.*;

public interface SLToolbarExtension {
    public JComponent getToolbarElement(ScrollableLogView.Context context);
}
