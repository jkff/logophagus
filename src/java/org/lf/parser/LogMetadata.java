package org.lf.parser;

public interface LogMetadata {
		int getFieldCount();
		String getFieldName(int fieldIndex);
		int getFieldIndex(String fieldName);
		String[] getFieldNames();
}
