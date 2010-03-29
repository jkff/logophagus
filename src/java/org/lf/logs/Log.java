package org.lf.logs;


import java.io.IOException;

import org.lf.parser.LogMetadata;
import org.lf.parser.Position;

import com.sun.istack.internal.Nullable;


public interface Log {

    public LogMetadata getMetadata();
    
    /**
     * @param pos can be: 
     * 	- position from this log(then it will be returned as the result)
     *  - position of parent log ,for example parent log of filtered log, or one of parent logs in merged log 
     *  - null -> then function return null;
     * @return :
     *  - position that can be used in methods this.readRecord(), this.prev() , this.next() and this.convertToNative()
     *  - null, if argument was null or position fail @param requirements
     * @throws IOException if :
     *  - during position transformation occur IOException 
     */
    @Nullable
    public Position convertToNative(Position pos) throws IOException;

    /**
     * @return:
     *  - position of first record in this log
     *  - null if there is no records in log
     *  @throws IOException if :
     *  - some exception occurs during its work
     */
    @Nullable
    public Position first() throws IOException;

    /**
     * @return:
     *  - position of last record in this log( so this.readRecord() method can be called on it)
     *  - null if there is no records in log
     *  @throws IOException if :
     *  - some exception occurs during its work
     */
    @Nullable
    public Position last() throws IOException;

    /**
     * @param pos can be: 
     * 	- position from this log
     *  - null -> then function return null;
     * @return
     *  - position that can be used in methods this.readRecord(), this.prev() , this.next() and this.convertToNative(),
     *  	this position goes after input pos(so that pos == prev(next(pos)))
     *  - null ,if position equals last() position
     * @throws IOException if :
     *  - during position transformation occur IOException
     *  
     *   it is required from this method that invocation of following function return true if Position p != null and p != last():  
     *   boolean checkNext(p) {
     *   	Position p = next(p);
     *   	if (p.equals(last()) return true;
     *		if (p == null) return false;
     *   	checkNext(p);  
     *   } 
     */
    @Nullable
    public Position next(Position pos) throws IOException;

    /**
     * @param pos can be: 
     * 	- position from this log
     *  - null -> then function return null;
     * @return
     *  - position that can be used in methods this.readRecord(), this.prev() , this.next() and this.convertToNative(),
     *  	this position goes before input pos(so that pos == next(prev(pos)))
     *  	
     *  - null ,if position equals first() position
     * @throws IOException if :
     *  - during position transformation occur IOException
     *  
     *   it is required from this method that invocation of following function return true if Position p != null and p != first():  
     *   boolean checkPrev(p) {
     *   	Position p = prev(p);
     *   	if (p.equals(first()) return true;
     *		if (p == null) return false;
     *   	checkPrev(p);  
     *   } 
     */
    @Nullable
    public Position prev(Position pos) throws IOException;

    
    /**
     * @param pos can be: 
     * 	- position from this log
     *  - null -> then function return null;
     * @return
     *  - Record realization corresponding to pos (so equal pos always return equal Records) 
     *  - null , pos == null
     * @throws IOException if :
     *  - during reading occur IOException
     */
    public Record readRecord(Position pos) throws IOException;

    //returns name of this log
    public String toString();

}
