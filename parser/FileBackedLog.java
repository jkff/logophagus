package parser;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;

class FileBackedLog implements Log {
	private static class PhysicalPosition implements Position {
		long offsetBytes;
		PhysicalPosition(long offsetBytes) {
			this.offsetBytes = offsetBytes;
		}
	}
	
	
	private Parser parser;
	
	FileBackedLog(Parser in) {
		parser = in;
	}
	
	public Position getStart() {
		return new PhysicalPosition(0L);
	}
	public Position getEnd() {
		return new PhysicalPosition(...);
	}
	
	public Record readRecord(Position pos) {
		PhysicalPosition pp = (PhysicalPosition)pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		return parser.readRecord(is);
	}
	
	public Position next(Position pos) {
		PhysicalPosition pp = (PhysicalPosition)pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		return new PhysicalPosition(pp.offsetBytes + parser.findNextRecord(is));
	}
	
	public Position prev(Position pos) {
		PhysicalPosition pp = (PhysicalPosition)pos;
		ScrollableInputStream is = getInputStreamFrom(pp.offsetBytes);
		return new PhysicalPosition(pp.offsetBytes + parser.findPrevRecord(is));
	}
}