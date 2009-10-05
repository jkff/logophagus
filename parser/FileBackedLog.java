package parser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileBackedLog implements Log {
	private static class PhysicalPosition implements Position {
		long offsetBytes;
		PhysicalPosition(long offsetBytes) {
			this.offsetBytes = offsetBytes;
		}
	}
	
	private class ScrollableFileStream extends ScrollableInputStream {
		private InputStream is;
		private long bytesReaded;
		public ScrollableFileStream(Long offset) throws IOException {
			super();
			is = new FileInputStream(fileName);
			is.skip(offset);
			bytesReaded = offset;
		}
		
		public void finalize() throws IOException{
			is.close();
		}

		@Override
		public long scrollBack(long offset) throws IOException {
			is.close();
			is = new FileInputStream(fileName);
			if (bytesReaded > offset) {;
				bytesReaded -= offset;
				is.skip( bytesReaded );
				return offset;
			} else {
				long scrolled = bytesReaded;
				bytesReaded = 0;
				return scrolled;
			}
			
		}

		@Override
		public long scrollForward(long offset) throws IOException {
			int maxOffset = is.available();
			if (maxOffset > bytesReaded + offset) {
				is.skip(offset);
				bytesReaded += offset;
				return offset;
			}else {
				bytesReaded += maxOffset;
				is.skip(bytesReaded);
				return maxOffset;
			}
		}

		@Override
		public int read() throws IOException {
			bytesReaded++;
			return is.read();
		}

		@Override
		public int available() throws IOException{
			return is.available();
		}
		
	}
	
	private String fileName;
	private Parser parser;

	public FileBackedLog(String fileName, Parser in) throws Exception  {
		parser = in;
		this.fileName = fileName;
	}
	
	public Position getStart() {
		return new PhysicalPosition(0L);
	}
	
	public Position getEnd() throws IOException {
 		ScrollableInputStream is = new ScrollableFileStream(0L);
 		long maxSize = is.available();
 		return new PhysicalPosition(maxSize);
	}
	
	public Record readRecord(Position pos) throws IOException{
		PhysicalPosition pp = (PhysicalPosition)pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		return parser.readRecord(is);
	}
	
	public Position next(Position pos) throws IOException{
		PhysicalPosition pp = (PhysicalPosition)pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		long pOffset = parser.findNextRecord(is);
		if (pOffset == -1) { 
			pOffset = 0;
		}
		return new PhysicalPosition(pp.offsetBytes + pOffset );
	}
	
	public Position prev(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition)pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		long pOffset = parser.findPrevRecord(is);
		if (pOffset == -1) { 
			pOffset = 0;
		}
		return new PhysicalPosition(pp.offsetBytes - pOffset);
	}
	
	private ScrollableInputStream getInputStreamFrom(Long offset ) throws IOException{
		return new ScrollableFileStream(offset);
	}
}