package org.lf.parser;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ScrollableInputStream extends InputStream {
	public abstract long scrollBack(long offset) throws IOException;
	public abstract long scrollForward(long offset) throws IOException;
	public abstract void scrollTo(long newOffset) throws IOException;
}
