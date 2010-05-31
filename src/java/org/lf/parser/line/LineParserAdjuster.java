package org.lf.parser.line;

import org.lf.parser.Parser;
import org.lf.ui.components.common.ParserAdjuster;

import javax.swing.*;

public class LineParserAdjuster extends ParserAdjuster {

    public LineParserAdjuster() {
        this.add(new JLabel("Select this parser if you want simply look at log"));
        this.setAdjustmentValid(true);
    }

    @Override
    public Parser getParser() {
        return new LineParser();
    }
}
