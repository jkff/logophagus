package parser;

public interface Log {
	public Position getStart();
	public Position getEnd();
	public Position next(Position pos);
	public Position prev(Position pos);

	public Record readRecord(Position pos);
}
