package org.lf.parser;

public abstract class Sink {
    private StringBuilder contents = new StringBuilder();

    public    void          onChar(char c)  { contents.append(c); }
    
    protected void          resetContents() { contents = new StringBuilder(); }
    protected StringBuilder getContents()   { return contents; }

    public void recordBorder() {}
    public void fieldBreak() {}
    public void error() {}
}
