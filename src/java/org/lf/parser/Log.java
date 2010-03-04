package org.lf.parser;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface Log {
    public Field[] getFields();

	public Position first() throws IOException;

	public Position afterLast();

    @Nullable
	public Position next(Position pos) throws IOException;

    @Nullable
	public Position prev(Position pos) throws IOException;

	public Record readRecord(Position pos) throws IOException;
	
    public String toString();
	
}
