package org.lf.util;


import org.lf.parser.Record;

public class FieldFilter implements Filter<Record>{
	private String value;
	private int fieldINdex;
	
	public FieldFilter(int fieldIndex,String value){
		this.fieldINdex = fieldIndex;
		this.value = value;
	}
    
	public String toString(){
    	return value;
    }
	
    public boolean accepts(Record t) {
    		return t.get(fieldINdex).equals(value);
    }


}