package org.lf.plugins.display;

import javax.swing.*;

/**
 * Created on: 26.05.2010 15:13:07
 */
public class SideBySideView implements View {
    private JScrollPane component;
    private View va;
    private View vb;

    public SideBySideView(View va, View vb) {
        this.va = va;
        this.vb = vb;
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        panel.add(va.getComponent());
        panel.add(Box.createHorizontalStrut(12));
        panel.add(vb.getComponent());

        this.component = new JScrollPane(panel);
    }

    @Override
    public JComponent getComponent() {
        return component;
    }
}
