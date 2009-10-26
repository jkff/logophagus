package org.lf.parser;

public class Record {
	private String rawValue;

	public Record(String rawValue) {
        // No need to call a default super() constructor.
		super();
		this.rawValue = rawValue;
	}

	public String toString() {
		return rawValue;
	}
}
