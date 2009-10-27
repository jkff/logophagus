package org.lf.parser;

import java.io.IOException;
//import java.nio.charset.Charset;

public class LineParser implements Parser {
	//private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

	//private Charset charset = DEFAULT_CHARSET;

	/*
	 * return -1 if eof
	 */
    // Hey, you're mixing two ways of reporting an error here:
    //  - EOF is reported with a returned "-1"
    //  - All other errors (for example, an IO error other than EOF)
    //    are reported with a thrown exception.
    // What is the motivation for making the client deal with two ways
    // that an error can reported?
	public long findNextRecord(ScrollableInputStream is) throws IOException {
		int i;
		int realData=0;
		long offset = 0;
		while ((i = is.read()) != (int) '\n') {
			if (i == -1)
				break;
				//break ;
            // Why two different spaces here, instead of
            // Character.isWhitespace or something like that?
            // (there is also Character.getType() - yes, unicode
            // is messy and hard to get right :) )
            // Generally, why are you bothering with spaces at all?
            // Shouldn't you only care about line breaks?
			
			//if record ends with eof and not with '\n'
			//we must check what exactly we read()
			//log can be like that one:
			//....
			//data data data...'\n'
			//"				  "'\eof'
			
			if (!Character.isWhitespace((char)i))
				++realData;
			offset++;
		}
		offset++;
		is.scrollBack(offset);
		if (i == -1 && realData == 0 ) return -1;
		return offset;
	}

    // Same here
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
        // Hehe. The "-1" now turns into an IO exception.
        // This code would not be necessary if the exception was thrown
        // in findNextRecord. 
		if (offset == -1)
			throw new IOException("Can't read after eof");
		byte[] buf = new byte[(int) offset];
		int n = is.read(buf);
		return new Record(new String(buf, 0, n));
	}
}
