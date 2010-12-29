package org.lf.parser.csv;

import org.lf.encoding.ScrollableReader;
import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.parser.CharStream;
import org.lf.parser.Parser;
import org.lf.util.CharVector;
import org.lf.util.ReverseCharVector;

import java.io.IOException;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

class CSVParser implements Parser {
    public final static char DEFAULT_RECORD_DELIMITER = '\n';
    public final static char DEFAULT_FIELD_DELIMITER = ',';
    public final static char DEFAULT_QUOTE_CHARACTER = '"';

    private final char recordDelimiter;
    private final char fieldDelimiter;
    private final char quoteCharacter;
    private final Format csvFormat;

    public CSVParser() {
        this(null, DEFAULT_RECORD_DELIMITER, DEFAULT_FIELD_DELIMITER, DEFAULT_QUOTE_CHARACTER);
    }

    public CSVParser(Format singleFormat) {
        this(singleFormat, DEFAULT_RECORD_DELIMITER, DEFAULT_FIELD_DELIMITER, DEFAULT_QUOTE_CHARACTER);
    }

    public CSVParser(Format singleFormat, char recordDelimiter, char fieldDelimiter, char quoteCharacter) {
        this.recordDelimiter = recordDelimiter;
        this.fieldDelimiter = fieldDelimiter;
        this.quoteCharacter = quoteCharacter;
        this.csvFormat = singleFormat;
    }

    @Override
    public long findNextRecord(ScrollableReader reader) throws IOException {
        return findBorder(forward(reader), new Forward(), new Sink(new CharVector())) ? reader.getCurrentOffset() : -1;
    }

    @Override
    public long findPrevRecord(ScrollableReader reader) throws IOException {
        if (reader.prev() == -1) return -1;
        ReverseCharVector reverseCharVector = new ReverseCharVector();
        if (!findBorder(backward(reader), new Backward(), new Sink(reverseCharVector))) {
            return -1;
        }
        if (reader.getCurrentOffset() != 0) {
            reader.next();
        }
        return reader.getCurrentOffset();
    }


    private boolean findBorder(
            CharStream stream,
            TransitionFunction<State, SymbolType> tf,
            Sink sink) throws IOException {
        State state = State.RECORD_BORDER;
        int offset = 0;
        do {
            int i = stream.next();
            if (i == -1) {
                sink.recordBorder();
                return offset != 0;
            }
            offset++;
            char c = (char) i;
            SymbolType s =
                    (c == recordDelimiter) ? SymbolType.RECORD_DELIMITER :
                    (c == fieldDelimiter) ? SymbolType.FIELD_DELIMITER :
                    (c == quoteCharacter) ? SymbolType.QUOTE :
                    Character.isSpaceChar(c) ? SymbolType.SPACE:
                    SymbolType.OTHER;

            state = tf.next(state, s);

            sink.onRawChar(c);

            switch (state) {
                case NON_QUOTED_FIELD:
                    sink.onChar(c, false);
                    break;
                case QUOTED_FIELD:
                    sink.onChar(c, true);
                    break;
                case DOUBLE_QUOTE:
                    sink.onChar(c , true);
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

        return true;
    }


    private CharStream forward(final ScrollableReader reader) {
        return new CharStream() {
            public int next() throws IOException {
                return reader.next();
            }
        };
    }

    private CharStream backward(final ScrollableReader reader) {
        return new CharStream() {
            public int next() throws IOException {
                return reader.prev();
            }
        };
    }

    @Override
    public Record readRecord(ScrollableReader reader) throws IOException {
        final List<CharSequence> fields = newList();

        final StringBuilder rawString = new StringBuilder();

        findBorder(forward(reader), new Forward(),
                new Sink(new CharVector()) {
                    public void fieldBreak() {
                        fields.add(isQuoted ? charVector.toString() : charVector.toString().trim());
                        charVector.clear();
                    }

                    public void recordBorder() {
                        fields.add(isQuoted ? charVector.toString() : charVector.toString().trim());
                        charVector.clear();
                    }

                    public void onRawChar(char c) {
                        rawString.append(c);
                    }
                });

        return new CSVRecord(rawString.toString(), fields);
    }


    private class CSVRecord implements Record {
        private final CharSequence rawString;
        private final CharSequence[] cells;
        private final boolean matchesFormat;

        private CSVRecord(CharSequence rawString, List<CharSequence> strFields) {
            this.rawString = rawString;
            matchesFormat = (strFields.size() == CSVParser.this.csvFormat.getFields().length);

            this.cells = new String[matchesFormat ? strFields.size() : 1];

            if (matchesFormat) {
                for (int i = 0; i < cells.length; i++)
                    cells[i] = strFields.get(i);
            } else {
                StringBuilder strB = new StringBuilder();
                for (CharSequence cur : strFields) {
                    strB.append(cur);
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
        public CharSequence getCell(int i) {
            return cells[i];
        }

        @Override
        public Format getFormat() {
            return matchesFormat ? CSVParser.this.csvFormat : Format.UNKNOWN_FORMAT;
        }

        @Override
        public CharSequence getRawString() {
            return rawString;
        }
    }

    @Override
    public Format[] getFormats() {
        return new Format[]{csvFormat};
    }

}
