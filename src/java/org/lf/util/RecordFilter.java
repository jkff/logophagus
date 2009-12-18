package org.lf.util;

import org.lf.parser.Record;

public class RecordFilter implements Filter<Record> {
	private String substring;
	
	public RecordFilter(String sub){
		this.substring = sub;
	}
    
	public String toString(){
    	return substring;
    }
	
    public boolean accepts(Record t) {
    	for (int i=0; i< t.size(); ++i){
    		if (t.get(i).contains(substring))
    			return true;
    	}
		return false;
	}

}
