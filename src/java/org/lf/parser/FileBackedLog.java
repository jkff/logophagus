package java.org.lf.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileBackedLog implements Log {
	private static class PhysicalPosition implements Position {
		long offsetBytes;

		PhysicalPosition(long offsetBytes) {
			this.offsetBytes = offsetBytes;
		}
	}

	//  Implements with RandomAccessFile
	private class ScrollableFileStream extends ScrollableInputStream {
		private RandomAccessFile data;

		public ScrollableFileStream(long offset) throws IOException {
			super();
			data = new RandomAccessFile(fileName,"r");
			data.seek(offset);
		}

		public void close() throws IOException {
			data.close();
		}

		@Override
		public long scrollBack(long offset) throws IOException {
			if (data.getFilePointer() > offset) {
				data.seek(data.getFilePointer() - offset);
				return offset;
			} else {
				long scrolled = data.getFilePointer();
				data.seek(0L);
				return scrolled;
			}

		}

		@Override
		public long scrollForward(long offset) throws IOException {
			if ( offset + data.getFilePointer() <= data.length()){
				data.seek(offset + data.getFilePointer());
				return offset;
			} else {
				long scrolled = data.length() - data.getFilePointer();
				data.seek(data.length());
				return scrolled;
			}
		}

		@Override
		public int read() throws IOException {
			return data.read();
		}

	}

	private String fileName;

	private Parser parser;

	public FileBackedLog(String fileName, Parser in) throws Exception {
		parser = in;
		this.fileName = fileName;
	}

	public Position getStart() {
		return new PhysicalPosition(0L);
	}

	public Position getEnd() throws IOException {
		long maxSize = new File(fileName).length();
		return new PhysicalPosition(maxSize);
	}

	public Record readRecord(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		try {
			Record r = parser.readRecord(is); 
			is.close();
			return r;
		}catch (IOException e){
			is.close();
			throw e ;
		}
	}

	public Position next(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		long offset = parser.findNextRecord(is);
		is.close();
		if (offset == -1) {
			return pos;
		}
		return new PhysicalPosition(pp.offsetBytes + offset);
	}

	public Position prev(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		long offset = parser.findPrevRecord(is);
		is.close();
		if (offset == -1) {
			return pos;
		}
		return new PhysicalPosition(pp.offsetBytes - offset);
	}

	private ScrollableInputStream getInputStreamFrom(Long offset)
			throws IOException {
		return new ScrollableFileStream(offset);
	}
}