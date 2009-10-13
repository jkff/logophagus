package org.lf.parser;

import java.io.IOException;
//import java.nio.charset.Charset;

public class LineParser implements Parser {
	//private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

	//private Charset charset = DEFAULT_CHARSET;

	/*
	 * return -1 if eof
	 * 
	 */
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		int i;
		int realData=0;
		long offset = 0;
		while ((i = is.read()) != (int) '\n') {
			if (i == -1)
				break;
			if ((char)i != ' ' || (char)i != '	')
				++realData;
			offset++;
		}
		offset++;
		is.scrollBack(offset);
		if (i == -1 && realData == 0 ) return -1;
		return offset;
	}

	public long findPrevRecord(ScrollableInputStream is) throws IOException {
		long scrolled = is.scrollBack(2);
		if (scrolled != 2) {
			is.scrollForward(scrolled);
			return -1;
		}

		long offset = 0;
		while (is.read() != (int) '\n') {
			scrolled = is.scrollBack(2);
			if (scrolled != 2) {
				is.scrollForward(scrolled);
				offset+=scrolled;
				break;
			}
			offset++;
		}

		offset++;
		is.scrollForward(offset);
		return offset;
	}

	public Record readRecord(ScrollableInputStream is) throws IOException {
		long offset = findNextRecord(is);
		if (offset == -1)
			throw new IOException("Can't read after eof");
		byte[] buf = new byte[(int) offset];
		int n = is.read(buf);
		return new Record(new String(buf, 0, n));
	}
}
