package org.lf.parser;

import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import java.io.IOException;

public class FileBackedLog implements Log {
	private RandomAccessFileIO file;

	private Parser parser;
	private ScrollableInputStream is;

	private static class PhysicalPosition implements Position {
		long offsetBytes;

		@Override
		public int hashCode() {
			// TODO think about better solution
			return (int)offsetBytes;
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && obj.getClass() == PhysicalPosition.class && 
				((PhysicalPosition)obj).offsetBytes == this.offsetBytes;
		}

		PhysicalPosition(long offsetBytes) {
			this.offsetBytes = offsetBytes;
		}

		@Override
		public String toString() {
			return "Phis. Pos. = " + offsetBytes;
		}
		
		
	}
	
	public FileBackedLog(String fileName, Parser in) throws IOException {
		parser = in;
		this.file = new MappedFile(fileName);
		is = file.getInputStreamFrom(0L);
	}

	@Override
	public Position first() {
		return new PhysicalPosition(0L);
	}
	
	@Override
	public Position last() throws IOException {
		long maxSize = file.length();
		return prev(new PhysicalPosition(maxSize));
	}

	@Override
	synchronized public Record readRecord(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.scrollTo(pp.offsetBytes);
		return parser.readRecord(is);
	}
	
	@Override
	synchronized public Position next(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.scrollTo(pp.offsetBytes);
		long offset = parser.findNextRecord(is);
		return new PhysicalPosition(pp.offsetBytes + offset);
	}

	@Override
	synchronized public Position prev(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.scrollTo(pp.offsetBytes);
		long offset = parser.findPrevRecord(is);
		return new PhysicalPosition(pp.offsetBytes - offset);
	}

	@Override
	public String toString() {
		String fileName = file.getFileName(); 
		return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
	}
	
}