package org.lf.ui.components.plugins.scrollablelog.extension.clipboard;

import org.lf.ui.components.plugins.scrollablelog.ScrollableLogModel;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class CopyToClipboardExtension implements SLInitExtension {
    @Override
    public void init(final ScrollableLogPanel.Context context) {
        context.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
                    ScrollableLogModel model = context.getModel();
                    int[] sind = context.getSelectedIndexes();
                    StringBuilder res = new StringBuilder();
                    for(int i : sind) {
                        res.append(model.getRecord(i).getRawString()).append("\n");
                    }

                    StringSelection data = new StringSelection(res.toString());

                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
                    e.consume();
                }
            }
        });
    }
}
