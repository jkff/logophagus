package org.lf.parser;

import java.io.IOException;

// TODO: Consider using .equals() on positions for testing whether a position
// is at start or at end of a log.

public interface Log {
	public Position getStart() throws IOException;

	public Position getEnd() throws IOException;

	public Position next(Position pos) throws IOException;

	public Position prev(Position pos) throws IOException;

	public Record readRecord(Position pos) throws IOException;
	
    public String toString();
	
}
