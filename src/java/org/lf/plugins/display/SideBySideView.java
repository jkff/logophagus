package org.lf.plugins.display;

import org.lf.util.Pair;

import javax.swing.*;

import static org.lf.util.CollectionFactory.pair;

/**
 * Created on: 26.05.2010 15:13:07
 */
public class SideBySideView implements View<Pair<Object,Object>> {
    private JComponent component;
    private View va;
    private View vb;

    public SideBySideView(View va, View vb) {
        this.va = va;
        this.vb = vb;
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(va.getComponent());
        split.setRightComponent(vb.getComponent());
        split.setResizeWeight(0.5);

        this.component = split;
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
