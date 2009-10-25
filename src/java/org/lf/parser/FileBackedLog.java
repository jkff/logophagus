package org.lf.parser;

import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import java.io.IOException;

public class FileBackedLog implements Log {
	private static class PhysicalPosition implements Position {
		long offsetBytes;

		PhysicalPosition(long offsetBytes) {
			this.offsetBytes = offsetBytes;
		}
	}

	private RandomAccessFileIO file;

	private Parser parser;
	private ScrollableInputStream is;
	
	public FileBackedLog(String fileName, Parser in) throws Exception {
		parser = in;
		this.file = new MappedFile(fileName);
		is = file.getInputStreamFrom(0L);
	}

	public Position getStart() {
		return new PhysicalPosition(0L);
	}

	public Position getEnd() throws IOException {
		long maxSize = file.length();
		return new PhysicalPosition(maxSize);
	}

	synchronized public Record readRecord(Position pos) throws Exception {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.shiftTo(pp.offsetBytes);
		try {
			Record r = parser.readRecord(is);
			return r;
		} catch (Exception e) {
			is.close();
			throw e;
		}
	}

	synchronized public Position next(Position pos) throws Exception {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.shiftTo(pp.offsetBytes);
		long offset = parser.findNextRecord(is);
		if (offset == -1) {
			return pos;
		}
		return new PhysicalPosition(pp.offsetBytes + offset);
	}

	synchronized public Position prev(Position pos) throws Exception {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.shiftTo(pp.offsetBytes);
		long offset = parser.findPrevRecord(is);
		if (offset == -1) {
			return pos;
		}
		return new PhysicalPosition(pp.offsetBytes - offset);
	}
}