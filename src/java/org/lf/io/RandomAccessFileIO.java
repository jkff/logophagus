package org.lf.io;

import java.io.IOException;

import org.lf.parser.ScrollableInputStream;

/**
 * User: jkff Date: Oct 13, 2009 Time: 3:22:34 PM
 */
public interface RandomAccessFileIO {
	ScrollableInputStream getInputStreamFrom(long offset) throws IOException;
	long length();
}
