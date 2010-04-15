package org.lf.parser;

import org.lf.ui.components.dialog.ParserSetup;

import javax.swing.*;

public abstract class ParserAdjuster extends JPanel{
    protected ParserSetup parent;

    public abstract Parser getParser();
    
    public void setParent(ParserSetup parent) {
        this.parent = parent;
        updateComponents();
    }

    protected abstract void updateComponents();
}
