package org.lf.parser;


import java.io.IOException;

import com.sun.istack.internal.Nullable;

public interface Log {
	
    public Field[] getFields();

	public Position first() throws IOException;

	public Position last() throws IOException;

    @Nullable
	public Position next(Position pos) throws IOException;

    @Nullable
	public Position prev(Position pos) throws IOException;

	public Record readRecord(Position pos) throws IOException;
	
    public String toString();
	
}
