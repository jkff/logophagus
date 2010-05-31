package org.lf.plugins.display;

import org.lf.util.Pair;

import javax.swing.*;

import static org.lf.util.CollectionFactory.pair;

/**
 * Created on: 26.05.2010 15:13:07
 */
public class SideBySideView implements View<Pair<Object,Object>> {
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

    @Override
    public Pair<Object, Object> getState() {
        return pair(va.getState(), vb.getState());
    }

    @Override
    public void restoreState(Pair<Object, Object> state) {
        va.restoreState(state.first);
        vb.restoreState(state.second);
    }
}
