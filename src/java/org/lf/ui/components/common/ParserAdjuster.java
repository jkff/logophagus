package org.lf.ui.components.common;

import org.lf.parser.Parser;

import javax.swing.*;

public abstract class ParserAdjuster extends JPanel {
    private boolean validAdjustment;

    public boolean isValidAdjustment() {
        return validAdjustment;
    }

    public abstract Parser getParser();

    protected void setValidAdjust(boolean isValid) {
        boolean oldVal = validAdjustment;
        validAdjustment = isValid;
        firePropertyChange("validAdjustment", oldVal, validAdjustment);
    }

}
