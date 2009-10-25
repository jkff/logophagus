package org.lf.parser;



public interface Log {
	public Position getStart() throws Exception;

	public Position getEnd() throws Exception;

	public Position next(Position pos) throws Exception;

	public Position prev(Position pos) throws Exception;

	public Record readRecord(Position pos) throws Exception;
}
