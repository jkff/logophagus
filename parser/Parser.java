package parser;

import java.io.InputStream;

public interface Parser {
	Record readRecord(ScrollableInputStream is);
	/**
	 * Find out the smallest positive offset from the current position 
	 * in 'is' such that there is a separator just before the offset and a
	 * record after it.
	 * This is the offset of the next record in 'is'.
	 */
	long findNextRecord(ScrollableInputStream is);
	/**
	 * Find out the smallest (by absolute value) negative offset from 
	 * the current position in 'is' such that there is a separator 
	 * just before the offset and a record after it.
	 * This is the offset of the previous record in 'is'.
	 */
	long findPrevRecord(ScrollableInputStream is);
}
