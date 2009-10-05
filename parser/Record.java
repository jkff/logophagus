package parser;

public class Record {
	private String rawValue;

	public Record(String rawValue) {
		super();
		this.rawValue = rawValue;
	}
	public String toString(){
		return rawValue;
	}
}
