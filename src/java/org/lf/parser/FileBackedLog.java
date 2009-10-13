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

	public FileBackedLog(String fileName, Parser in) throws Exception {
		parser = in;
        this.file = new MappedFile(fileName);
	}

	public Position getStart() {
		return new PhysicalPosition(0L);
	}

	public Position getEnd() throws IOException {
		long maxSize = file.length();
		return new PhysicalPosition(maxSize);
	}

	public Record readRecord(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
        ScrollableInputStream is = file.getInputStreamFrom(pp.offsetBytes);
		try {
			Record r = parser.readRecord(is); 
			is.close();
			return r;
		} catch (IOException e) {
			is.close();
			throw e ;
		}
	}

	public Position next(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
        ScrollableInputStream is = file.getInputStreamFrom(pp.offsetBytes);
		long offset = parser.findNextRecord(is);
		is.close();
		if (offset == -1) {
			return pos;
		}
		return new PhysicalPosition(pp.offsetBytes + offset);
	}

	public Position prev(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
        ScrollableInputStream is = file.getInputStreamFrom(pp.offsetBytes);
		long offset = parser.findPrevRecord(is);
		is.close();
		if (offset == -1) {
			return pos;
		}
		return new PhysicalPosition(pp.offsetBytes - offset);
	}
}