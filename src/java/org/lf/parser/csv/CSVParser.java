package org.lf.parser.csv;

import java.io.IOException;
import java.util.*;

import org.lf.logs.Cell;
import org.lf.logs.Record;
import org.lf.logs.Cell.Type;
import org.lf.parser.*;
import org.lf.parser.LogMetadata;

import static org.lf.util.CollectionFactory.newList;

public class CSVParser implements Parser {
    private final char recordDelimeter;
    private final char fieldDelimeter;
    private final char quoteCharacter;
    private final char escapeCharacter;
    private final LogMetadata logMetadata;
    
    public CSVParser(LogMetadata logMetadata) {
        this(logMetadata,'\n', ',', '"', '\\');
    }

    public CSVParser(LogMetadata logMetadata, char recordDelimeter, char fieldDelimeter, char quoteCharacter, char escapeCharacter) {
        this.recordDelimeter = recordDelimeter;
        this.fieldDelimeter = fieldDelimeter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
        this.logMetadata = logMetadata;
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
            offset++;
            if (i == -1) {
                sink.recordBorder();
                return offset;
            }

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
        final List<String> fields = newList();

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
        private final Cell[] cells;

        private CSVRecord(List<String> strFields) {
        	boolean matchesFormat = strFields.size() == logMetadata.getFieldCount();
            this.cells = new Cell[strFields.size()];

        	for (int i = 0; i < cells.length; i++) {
        		cells[i] = new CSVCell(matchesFormat ? logMetadata.getFieldName(i) : "Field "+ i,
        				strFields.get(i), 
        				Type.TEXT, 
        				i);
			}
        }

		@Override
		public Cell[] getCells() {
			return cells;
		}
    }

    private static class CSVCell extends Cell {
		private final int index;
		private final String name;
		private final Type type;
		private final Object value;

		private CSVCell(String name, Object value, Type type, int index ) {
			this.name = name;
			this.value = value;
			this.type = type;
			this.index = index;
		}

		@Override
		public int getIndexInRecord() {
			return index;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Object getValue() {
			return value;
		}
    	
    }
    
    private static class Nop extends Sink {
        public void onChar(char c) { }
    }

	@Override
	public LogMetadata getLogMetadata() {
		return logMetadata;
	}
}
