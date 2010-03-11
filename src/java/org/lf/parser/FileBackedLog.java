package org.lf.parser;

import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FileBackedLog implements Log {
	private final RandomAccessFileIO file;
	private final Parser parser;
	private final ScrollableInputStream is;
	private Field[] fields; 
	               
	private static class PhysicalPosition implements Position {
		long offsetBytes;

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
	}
	
	public FileBackedLog(String fileName, Parser in) throws IOException {
		this.parser = in;
		this.file = new MappedFile(fileName);
		this.is = file.getInputStreamFrom(0L);
		this.fields = new Field[0];
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
		Record rec = parser.readRecord(is);
		validateFieldsFromRecord(rec);
		return rec;
	}
	
	@Override
	synchronized public Position next(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.scrollTo(pp.offsetBytes);
		long offset = parser.findNextRecord(is);
		if (offset == 0) 
			return null;
		return new PhysicalPosition(pp.offsetBytes + offset);
	}

	@Override
	synchronized public Position prev(Position pos) throws IOException {
		PhysicalPosition pp = (PhysicalPosition) pos;
		is.scrollTo(pp.offsetBytes);
		long offset = parser.findPrevRecord(is);
		if (offset == 0) 
			return null;
		return new PhysicalPosition(pp.offsetBytes - offset);
	}

	@Override
	public String toString() {
		return file.getFileName();
	}

	@Override
	synchronized public Field[] getFields() {
		return fields;
	}
	
	private void validateFieldsFromRecord(Record rec) {
		if (rec.size() <= fields.length) return;
		List<Field> fieldsList = new LinkedList<Field>();

		for (int i=0; i<rec.size(); ++i) {
			final int temp = i;
			fieldsList.add(new Field() {

				@Override
				public Type getType() {
					return Type.TEXT;
				}
				
				@Override
				public String getName() {
					return "Field " + temp;
				}
			});
		}
		fields = fieldsList.toArray(new Field[0]);
	}
	
}