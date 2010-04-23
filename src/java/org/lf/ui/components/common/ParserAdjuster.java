package org.lf.ui.components.common;

import org.lf.parser.Parser;

import javax.swing.*;

public abstract class ParserAdjuster extends JPanel {
    private boolean validAdjust;

    public boolean isValidAdjust() {
        return validAdjust;
    }

    public abstract Parser getParser();

    protected void setValidAdjust(boolean isValid) {
        boolean oldVal = validAdjust;
        validAdjust = isValid;
        firePropertyChange("validAdjust", oldVal, validAdjust);
    }

}
