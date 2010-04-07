package org.lf.logs;
import java.util.Map;
import static org.lf.util.CollectionFactory.newHashMap;;

public class UnknownFormat implements Format {
	private final Field[] fields;
	
	private static final Map<Integer, UnknownFormat > sizeToFields = newHashMap();
	
	private UnknownFormat(int fieldCount){
		this.fields = new Field[fieldCount];
		for (int i = 0; i< fieldCount; ++i) {
			fields[i] = new Field("Unknown field" + i);
		}
	}
	
	public static UnknownFormat getInstance(int fieldCount) {
		if (sizeToFields.containsKey(fieldCount)) 
			return sizeToFields.get(fieldCount);
		sizeToFields.put(fieldCount, new UnknownFormat(fieldCount));
		return sizeToFields.get(fieldCount);
	}
	
	@Override
	public Field[] getFields() {
		return this.fields;
	}

}
