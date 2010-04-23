package org.lf.parser.csv;

import static org.lf.parser.csv.State.*;
import static org.lf.parser.csv.SymbolType.*;

class Forward implements TransitionFunction<State, SymbolType> {
    private static final State[] STATES = State.values();

    public State next(State cur, SymbolType in) {
        return STATES[symbol2state2state[in.ordinal()][cur.ordinal()]];
    }

    private static void put(SymbolType sym, State src, State dest) {
        symbol2state2state[sym.ordinal()][src.ordinal()] = dest.ordinal();
    }

    private static int[][] symbol2state2state = new int[SymbolType.values().length][STATES.length];


    static {
        put(QUOTE, FIELD, QUOTE_BEGIN);
        put(ESCAPE, FIELD, FIELD);
        put(FIELD_DELIMITER, FIELD, BETWEEN_FIELDS);
        put(OTHER, FIELD, FIELD);
        put(RECORD_DELIMITER, FIELD, RECORD_BORDER);

        put(QUOTE, IN_QUOTE, QUOTE_END);
        put(ESCAPE, IN_QUOTE, IN_QUOTE_ESCAPE);
        put(FIELD_DELIMITER, IN_QUOTE, IN_QUOTE);
        put(RECORD_DELIMITER, IN_QUOTE, IN_QUOTE);
        put(OTHER, IN_QUOTE, IN_QUOTE);

        put(QUOTE, QUOTE_END, DOUBLE_QUOTE);
        put(ESCAPE, QUOTE_END, ERROR);
        put(FIELD_DELIMITER, QUOTE_END, BETWEEN_FIELDS);
        put(OTHER, QUOTE_END, FIELD);
        put(RECORD_DELIMITER, QUOTE_END, RECORD_BORDER);

        put(QUOTE, QUOTE_BEGIN, QUOTE_END);
        put(ESCAPE, QUOTE_BEGIN, IN_QUOTE_ESCAPE);
        put(FIELD_DELIMITER, QUOTE_BEGIN, IN_QUOTE);
        put(OTHER, QUOTE_BEGIN, IN_QUOTE);
        put(RECORD_DELIMITER, QUOTE_BEGIN, IN_QUOTE);

        put(QUOTE, DOUBLE_QUOTE, QUOTE_END);
        put(ESCAPE, DOUBLE_QUOTE, IN_QUOTE_ESCAPE);
        put(FIELD_DELIMITER, DOUBLE_QUOTE, IN_QUOTE);
        put(OTHER, DOUBLE_QUOTE, IN_QUOTE);
        put(RECORD_DELIMITER, DOUBLE_QUOTE, IN_QUOTE);

        put(QUOTE, RECORD_BORDER, QUOTE_BEGIN);
        put(ESCAPE, RECORD_BORDER, FIELD);
        put(FIELD_DELIMITER, RECORD_BORDER, BETWEEN_FIELDS);
        put(OTHER, RECORD_BORDER, FIELD);
        put(RECORD_DELIMITER, RECORD_BORDER, RECORD_BORDER);

        put(RECORD_DELIMITER, BETWEEN_FIELDS, RECORD_BORDER);
        put(ESCAPE, BETWEEN_FIELDS, FIELD);
        put(FIELD_DELIMITER, BETWEEN_FIELDS, BETWEEN_FIELDS);
        put(OTHER, BETWEEN_FIELDS, FIELD);
        put(QUOTE, BETWEEN_FIELDS, QUOTE_BEGIN);

        put(QUOTE, IN_QUOTE_ESCAPE, IN_QUOTE);
        put(ESCAPE, IN_QUOTE_ESCAPE, IN_QUOTE);
        put(FIELD_DELIMITER, IN_QUOTE_ESCAPE, IN_QUOTE);
        put(OTHER, IN_QUOTE_ESCAPE, IN_QUOTE);
        put(RECORD_DELIMITER, IN_QUOTE_ESCAPE, IN_QUOTE);

    }
}
