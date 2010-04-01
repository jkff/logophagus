package org.lf.logs;


import java.io.IOException;

import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;


public interface Log {

    /**
     * @param pos can be: 
     * 	- position from this log(then it will be returned as the result)
     *  - position of parent log, for example parent log of filtered log, or one of parent logs in merged log 
     *  - null -> then function returns null;
     * @return :
     *  - position 'pos' that can be used in methods this.readRecord(), this.prev(), this.next() and this.convertToNative()
     *    such that this.readRecord(pos) returns the same (if possible) record as pos.getCorrespondingLog().readRecord(pos).
     *  - null, if argument was null or position fail @param requirements
     */
    @Nullable
    public Position convertToNative(Position pos) throws IOException;

    /**
     * @return
     *  - position of first record in this log
     *  - null if there are no records in this log
     */
    @Nullable
    public Position first() throws IOException;

    /**
     * @return
     *  - position of last record in this log (so this.readRecord() method can be called on it)
     *  - null if there are no records in the log
     */
    @Nullable
    public Position last() throws IOException;

    /**
     * @param pos can be: 
     * 	- position from this log
     *  - null -> then the function returns null;
     * @return
     *  - position that can be used in methods this.readRecord(), this.prev() , this.next() and this.convertToNative(),
     *    this position goes after input pos(so that pos.equals(prev(next(pos))))
     *  - null, if position equals last()
     */
    @Nullable
    public Position next(Position pos) throws IOException;

    /**
     * @param pos can be: 
     * 	- position from this log
     *  - null -> then function return null;
     * @return
     *  - position that can be used in methods this.readRecord(), this.prev() , this.next() and this.convertToNative(),
     *  	this position goes before input pos (so that pos.equals(next(prev(pos))))
     *  - null, if position equals first()
     */
    @Nullable
    public Position prev(Position pos) throws IOException;

    
    /**
     * @param pos can be: 
     * 	- position from this log
     *  - null -> then function return null;
     * @return
     *  - Record corresponding to pos (so equal pos always return equal Records) 
     *  - null, pos == null
     */
    public Record readRecord(Position pos) throws IOException;

    /**
     * returns name of this log
     */
    public String toString();

    public Format[] getFormats();
}
