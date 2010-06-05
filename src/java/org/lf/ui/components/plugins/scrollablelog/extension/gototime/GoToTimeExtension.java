package org.lf.ui.components.plugins.scrollablelog.extension.gototime;

import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created on: 05.06.2010 17:49:52
 */
public class GoToTimeExtension implements SLInitExtension {
    @Override
    public void init(final ScrollableLogPanel.Context context) {
        context.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_G) {
                    go(context);
                }
            }
        });
    }

    private void go(ScrollableLogPanel.Context context) {
        GoToTimeDialog dialog = new GoToTimeDialog(context.getModel());
        dialog.setVisible(true);
    }
}
