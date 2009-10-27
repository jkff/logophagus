package org.lf.parser;

import java.io.IOException;



public interface Log {
    // Why all these "throws Exception"?
    // This means, "Anything can happen, from a network error to a NullPointerException,
    // and you must handle everything the same way, because, after all, we won't tell you
    // which exceptions actually may be thrown and which can't".
    // The only places where "throws Exception" or "throws Throwable" are OK are places
    // where you really can't tell absolutely anything about what may be thrown and must
    // handle everything the same way. For example, one may introduce an interface
    // interface DangerousAction {void run() throws Exception;}
    // and introduce an API for working with dangerous actions. Then the client is free
    // to implement run() as dangerously as he likes.
    // But in places like the API of a Log, or of a Parser, hiding the range of possible
    // errors from the client is unacceptable.
    // If you're confused by too many exceptions that may be thrown, wrap them in
    // several groups of your own, for example, LogParsingException (that occurs when a log
    // is malformed), LogReadingException (that occurs when an I/O error happens while
    // reading a log) etc.
    // Generally, when writing a throws clause, think about how the error handling code may
    // look like. If it is "throws Exception", then is the code supposed to look like this?
    //
    // Position p;
    // try { p = log.getStart(); }
    // catch(Exception e) { /* So what are we supposed to do here? */ }
    //
    // In contrast:
    // try { p = log.getStart(); }
    // catch(LogParsingException e) {
    //     showMessageBox("Syntactic error in log at line " + e.getLineNumber());
    // } ...

	public Position getStart() throws IOException;

	public Position getEnd() throws IOException;

	public Position next(Position pos) throws IOException;

	public Position prev(Position pos) throws IOException;

	public Record readRecord(Position pos) throws IOException;
}
