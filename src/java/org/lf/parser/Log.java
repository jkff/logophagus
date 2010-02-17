package org.lf.parser;

import java.io.IOException;

public interface Log {
	public Position first() throws IOException;

	public Position last() throws IOException;

	public Position next(Position pos) throws IOException;

	public Position prev(Position pos) throws IOException;

	public Record readRecord(Position pos) throws IOException;
	
    public String toString();
	
}
