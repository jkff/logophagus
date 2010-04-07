package org.lf.logs;

import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import org.lf.parser.Parser;
import org.lf.parser.Position;
import org.lf.parser.ScrollableInputStream;

import java.io.IOException;

public class FileBackedLog implements Log {
	private final RandomAccessFileIO file;
	private final Parser parser;
	
	private class PhysicalPosition implements Position {
		final long offsetBytes;

		@Override
		public int hashCode() {
			return (int)(offsetBytes ^ (offsetBytes >>> 32));
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null &&
			obj.getClass() == PhysicalPosition.class &&
			((PhysicalPosition)obj).offsetBytes == this.offsetBytes;
		}

		PhysicalPosition(long offsetBytes) {
			this.offsetBytes = offsetBytes;
		}

		@Override
		public String toString() {
			return "Physical position " + offsetBytes;
		}

		@Override
		public Log getCorrespondingLog() {
			return FileBackedLog.this;
		}
	}

	public FileBackedLog(String fileName, Parser in) throws IOException {
		this(new MappedFile(fileName), in);
	}
	
	public FileBackedLog(RandomAccessFileIO io, Parser in) throws IOException {
		this.parser = in;
		this.file = io;
	}

	@Override
	public Position first() {
		return new PhysicalPosition(0L);
	}

	@Override
	public Position last() throws IOException {
		return prev(new PhysicalPosition(file.length()));
	}

	@Override
	public Record readRecord(Position pos) throws IOException {
		ScrollableInputStream is = null;
		try {
			is = file.getInputStreamFrom(((PhysicalPosition) pos).offsetBytes);
			Record rec = parser.readRecord(is);
			return rec;
		} finally {
			if (is != null) is.close(); 
		}
	}

	@Override
	public Position next(Position pos) throws IOException {
		ScrollableInputStream is = null;
		try {
			PhysicalPosition pp = (PhysicalPosition) pos;
			is = file.getInputStreamFrom(pp.offsetBytes);
			long offset = parser.findNextRecord(is);
			if (offset == 0)
				return null;
			return new PhysicalPosition(pp.offsetBytes + offset);
		} finally {
			if (is != null) is.close(); 
		}

	}

	@Override
	public Position prev(Position pos) throws IOException {
		ScrollableInputStream is = null;
		try {
			PhysicalPosition pp = (PhysicalPosition) pos;
			is = file.getInputStreamFrom(pp.offsetBytes);
			long offset = parser.findPrevRecord(is);
			if (offset == 0)
				return null;
			return new PhysicalPosition(pp.offsetBytes - offset);
		} finally {
			if (is != null) is.close(); 
		}

	}

	@Override
	public String toString() {
		return file.getFileName();
	}

	@Override
	public Position convertToNative(Position p) throws IOException {
		if (p.getCorrespondingLog() != this) return null;
		return p;
	}

	@Override
	public Format[] getFormats() {
		return parser.getFormats();
	}

}