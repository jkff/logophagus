package org.lf.parser;

public class Record {
	private String rawValue;

    public Record(String rawValue) {
        this.rawValue = rawValue;
    }

    public String toString() {
        return rawValue;
    }
}
