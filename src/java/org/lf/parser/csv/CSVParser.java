package org.lf.parser.csv;

import java.io.IOException;
import java.util.*;

import org.lf.parser.Parser;
import org.lf.parser.Record;
import org.lf.parser.ScrollableInputStream;

public class CSVParser implements Parser {
    private final char recordDelimeter;
    private final char fieldDelimeter;
    private final char quoteCharacter;
    private final char escapeCharacter;

    public CSVParser() {
        this('\n', ',', '"', '\\');
    }

    public CSVParser(char recordDelimeter, char fieldDelimeter, char quoteCharacter, char escapeCharacter) {
        this.recordDelimeter = recordDelimeter;
        this.fieldDelimeter = fieldDelimeter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
    }

	public long findNextRecord(ScrollableInputStream is) throws IOException {
        return findBorder(forward(is), new Forward(), new Nop());
    }

    public long findPrevRecord(ScrollableInputStream is) throws IOException {
		if (is.scrollBack(1) == 0)
			return 0;
        return findBorder(backward(is), new Backward(), new Nop());
	}
    
    
    private long findBorder(
            CharStream stream,
            TransitionFunction<State,SymbolType> tf,
            Sink sink) throws IOException
    {
        State state = State.RECORD_BORDER;
        int offset = 0;
        do {
            int i = stream.next();
            if (i == -1) { 
            	sink.recordBorder();
            	return offset;
            }
            offset++;            	
     
            char c = (char)i;
            SymbolType s =
                    (c == recordDelimeter) ? SymbolType.RECORD_DELIMITER :
                    (c == escapeCharacter) ? SymbolType.ESCAPE :
                    (c == fieldDelimeter)  ? SymbolType.FIELD_DELIMITER :
                    (c == quoteCharacter)  ? SymbolType.QUOTE :
                    SymbolType.OTHER;

            state = tf.next(state, s);
            
            switch(state) {
            case FIELD:             sink.onChar(c);     break;
            case IN_QUOTE:          sink.onChar(c);     break;
            case DOUBLE_QUOTE:      sink.onChar(c);     break;
            case BETWEEN_FIELDS:    sink.fieldBreak();  break;
            case ERROR:             sink.error();       break;
            case RECORD_BORDER:     sink.recordBorder();break;
            
            default:                                    break;
			}
        } while (state != State.RECORD_BORDER);
        
        return offset;
    }

    private interface Sink {
        void onChar(char c);
        void recordBorder();
		void fieldBreak();
        void error();
    }

    private CharStream forward(final ScrollableInputStream is) {
        return new CharStream() {
            public int next() throws IOException {
                return is.read();
            }
        };
    }
    private CharStream backward(final ScrollableInputStream is) {
        return new CharStream() {
            public int next() throws IOException {
                if (is.scrollBack(1) == 0)
                    return -1;
                int res = is.read();
                is.scrollBack(1);
                return res;
            }
        };
    }

	public Record readRecord(ScrollableInputStream is) throws IOException {
        final List<String> fields = new ArrayList<String>();

		findBorder(forward(is), new Forward(),
                new Sink() {
                    private StringBuilder sb = new StringBuilder();

                    public void onChar(char c) {
                        sb.append(c);
                    }
                    public void fieldBreak() {
                        fields.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    public void error() { }

                    public void recordBorder() {
                        fields.add(sb.toString());
					}
                });

        return new CSVRecord(fields);
	}
	
	private class CSVRecord implements Record {
		private final List<String> fields;

        private CSVRecord(List<String> fields) { this.fields = fields; }

        public String get(int index) { return fields.get(index); }
        public int    size()         { return fields.size();     }
	}

    private static class Nop implements Sink {
        public void onChar(char c) { }
        public void fieldBreak() { }
        public void error() { }
		public void recordBorder() {}
    }
}
