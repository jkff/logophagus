package org.lf.parser.csv;

import org.lf.util.CharVector;

public class Sink {
    protected final CharVector charVector;
    protected boolean isQuoted = false;

    public Sink(CharVector charVector) {
        this.charVector = charVector;
    }

    public void onChar(char c, boolean isQuoted) {
        charVector.add(c);
        this.isQuoted = isQuoted;
    }

    public void recordBorder() {
    }

    public void fieldBreak() {
    }

    public void error() {
    }

    public void onRawChar(char c) {

    }
}
