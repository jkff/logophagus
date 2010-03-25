package org.lf.parser;

public interface LogFormat {
		int getFieldCount();
		String getFieldName(int fieldIndex);
		int getFieldIndex(String fieldName);
		String[] getFieldNames();
}
