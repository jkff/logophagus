package org.lf.ui.components.common;

import org.lf.parser.Parser;

import javax.swing.*;

public abstract class ParserAdjuster extends JPanel {
    private boolean adjustmentValid;

    public boolean isAdjustmentValid() {
        return adjustmentValid;
    }

    public abstract Parser getParser();

    protected void setAdjustmentValid(boolean isValid) {
        boolean oldVal = adjustmentValid;
        adjustmentValid = isValid;
        firePropertyChange("adjustmentValid", oldVal, adjustmentValid);
    }

}
