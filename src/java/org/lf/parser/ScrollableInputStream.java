package org.lf.parser;
import java.io.InputStream;

public abstract class ScrollableInputStream extends InputStream {
	public abstract long scrollBack(long offset) throws Exception;
	public abstract long scrollForward(long offset) throws Exception;
	public abstract void shiftTo(long newOffset) throws Exception;
}
