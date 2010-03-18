package org.lf.logs;


import java.io.IOException;

import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;

public interface Log {

    public Field[] getFields();

    /**
     * Points to the first record in the log
     */
    public Position first() throws IOException;

    /**
     * Points to the last record in the log (not after it) 
     */
    public Position last() throws IOException;

    @Nullable
    public Position next(Position pos) throws IOException;

    @Nullable
    public Position prev(Position pos) throws IOException;

    public Record readRecord(Position pos) throws IOException;

    public String toString();

}
