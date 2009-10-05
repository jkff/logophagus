package parser;

import java.io.InputStream;

public abstract class ScrollableInputStream extends InputStream {
	public abstract void scrollBack(long offset);
	public abstract void scrollForward(long offset);
}
