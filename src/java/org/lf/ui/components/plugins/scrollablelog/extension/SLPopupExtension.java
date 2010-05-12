package org.lf.ui.components.plugins.scrollablelog.extension;

import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.util.HierarchicalAction;

public interface SLPopupExtension {
    HierarchicalAction getHierarchicalActionFor(ScrollableLogView.Context context);
}
