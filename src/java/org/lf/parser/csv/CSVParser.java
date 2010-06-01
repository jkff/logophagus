package org.lf.parser.csv;

import org.lf.io.ScrollableInputStream;
import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.parser.CharStream;
import org.lf.parser.Parser;
import org.lf.parser.Sink;

import java.io.IOException;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

class CSVParser implements Parser {
    public final static char DEFAULT_RECORD_DELIMITER = '\n';
    public final static char DEFAULT_FIELD_DELIMITER = ',';
    public final static char DEFAULT_QUOTE_CHARACTER = '"';
    public final static char DEFAULT_ESCAPE_CHARACTER = '\\';

    private final char recordDelimiter;
    private final char fieldDelimiter;
    private final char quoteCharacter;
    private final char escapeCharacter;
    private final Format csvFormat;

    public CSVParser() {
        this(null, DEFAULT_RECORD_DELIMITER, DEFAULT_FIELD_DELIMITER, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVParser(Format singleFormat) {
        this(singleFormat, DEFAULT_RECORD_DELIMITER, DEFAULT_FIELD_DELIMITER, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
    }

    public CSVParser(Format singleFormat, char recordDelimiter, char fieldDelimiter, char quoteCharacter, char escapeCharacter) {
        this.recordDelimiter = recordDelimiter;
        this.fieldDelimiter = fieldDelimiter;
        this.quoteCharacter = quoteCharacter;
        this.escapeCharacter = escapeCharacter;
        this.csvFormat = singleFormat;
    }

    public int findNextRecord(ScrollableInputStream is) throws IOException {
        return findBorder(forward(is), new Forward(), new Nop());
    }

    public int findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0)
            return 0;
        return findBorder(backward(is), new Backward(), new Nop());
    }


    private int findBorder(
            CharStream stream,
            TransitionFunction<State, SymbolType> tf,
            Sink sink) throws IOException {
        State state = State.RECORD_BORDER;
        int offset = 0;
        do {
            int i = stream.next();
            offset++;
            if (i == -1) {
                sink.recordBorder();
                return offset;
            }

            char c = (char) i;
            SymbolType s =
                    (c == recordDelimiter) ? SymbolType.RECORD_DELIMITER :
                            (c == escapeCharacter) ? SymbolType.ESCAPE :
                                    (c == fieldDelimiter) ? SymbolType.FIELD_DELIMITER :
                                            (c == quoteCharacter) ? SymbolType.QUOTE :
                                                    SymbolType.OTHER;

            state = tf.next(state, s);

            switch (state) {
                case FIELD:
                    sink.onChar(c);
                    break;
                case IN_QUOTE:
                    sink.onChar(c);
                    break;
                case DOUBLE_QUOTE:
                    sink.onChar(c);
                    break;
                case BETWEEN_FIELDS:
                    sink.fieldBreak();
                    break;
                case ERROR:
                    sink.error();
                    break;
                case RECORD_BORDER:
                    sink.recordBorder();
                    break;

                default:
                    break;
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
                return is.readBack();
            }
        };
    }

    public Record readRecord(ScrollableInputStream is) throws IOException {
        final List<String> fields = newList();

        findBorder(forward(is), new Forward(),
                new Sink() {
                    public void fieldBreak() {
                        fields.add(getContents().toString());
                        resetContents();
                    }

                    public void error() {
                    }

                    public void recordBorder() {
                        fields.add(getContents().toString());
                    }
                });

        return new CSVRecord(fields);
    }


    private class CSVRecord implements Record {
        private final String[] cells;
        private final boolean matchesFormat;

        private CSVRecord(List<String> strFields) {
            matchesFormat = (strFields.size() == CSVParser.this.csvFormat.getFields().length);

            this.cells = new String[matchesFormat ? strFields.size() : 1];

            if (matchesFormat) {
                for (int i = 0; i < cells.length; i++)
                    cells[i] = strFields.get(i);
            } else {
                StringBuilder strB = new StringBuilder();
                for (String string : strFields) {
                    strB.append(string);
                    strB.append(' ');
                }
                cells[0] = strB.toString();
            }
        }

        @Override
        public int getCellCount() {
            return cells.length;
        }

        @Override
        public String getCell(int i) {
            return cells[i];
        }

        @Override
        public Format getFormat() {
            return matchesFormat ? CSVParser.this.csvFormat : Format.UNKNOWN_FORMAT;
        }
    }

    private static class Nop extends Sink {
    }

    @Override
    public Format[] getFormats() {
        return new Format[]{csvFormat};
    }

}
