package org.lf.parser;

public abstract class Sink {
    public abstract void onChar(char c);
    public void recordBorder() {};
    public void fieldBreak() {};
    public void error() {};
}
