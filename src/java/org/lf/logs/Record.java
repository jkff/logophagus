package org.lf.logs;

import com.sun.istack.internal.Nullable;

//each Record consists from Fields
public interface Record {
    public int size();
    @Nullable
    public Field getField(int index);
    public Field[] getFields();
    
}
