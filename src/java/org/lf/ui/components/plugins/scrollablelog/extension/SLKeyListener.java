package org.lf.ui.components.plugins.scrollablelog.extension;

import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;

import java.awt.event.KeyEvent;

public interface SLKeyListener {
    /**
     * Invoked when a key has been typed.
     */
    public void keyTyped(KeyEvent e, ScrollableLogView.Context context);

    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(KeyEvent e, ScrollableLogView.Context context);

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e, ScrollableLogView.Context context);

}
