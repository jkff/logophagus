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
        put(QUOTE, NON_QUOTED_FIELD, ERROR);
        put(FIELD_DELIMITER, NON_QUOTED_FIELD, BETWEEN_FIELDS);
        put(OTHER, NON_QUOTED_FIELD, NON_QUOTED_FIELD);
        put(RECORD_DELIMITER, NON_QUOTED_FIELD, RECORD_BORDER);
        put(SPACE, NON_QUOTED_FIELD, NON_QUOTED_FIELD);

        put(QUOTE, QUOTED_FIELD, QUOTE_END);
        put(FIELD_DELIMITER, QUOTED_FIELD, QUOTED_FIELD);
        put(RECORD_DELIMITER, QUOTED_FIELD, QUOTED_FIELD);
        put(OTHER, QUOTED_FIELD, QUOTED_FIELD);
        put(SPACE, QUOTED_FIELD, QUOTED_FIELD);

        put(QUOTE, QUOTE_END, DOUBLE_QUOTE);
        put(FIELD_DELIMITER, QUOTE_END, BETWEEN_FIELDS);
        put(OTHER, QUOTE_END, ERROR);
        put(RECORD_DELIMITER, QUOTE_END, RECORD_BORDER);
        put(SPACE, QUOTE_END, SPACE_BETWEEN_FIELDS);

        put(QUOTE, QUOTE_BEGIN, QUOTE_END);
        put(FIELD_DELIMITER, QUOTE_BEGIN, QUOTED_FIELD);
        put(OTHER, QUOTE_BEGIN, QUOTED_FIELD);
        put(RECORD_DELIMITER, QUOTE_BEGIN, QUOTED_FIELD);
        put(SPACE, QUOTE_BEGIN, QUOTED_FIELD);

        put(QUOTE, DOUBLE_QUOTE, QUOTE_END);
        put(FIELD_DELIMITER, DOUBLE_QUOTE, QUOTED_FIELD);
        put(OTHER, DOUBLE_QUOTE, QUOTED_FIELD);
        put(RECORD_DELIMITER, DOUBLE_QUOTE, QUOTED_FIELD);
        put(SPACE, DOUBLE_QUOTE, QUOTED_FIELD);

        put(QUOTE, RECORD_BORDER, QUOTE_BEGIN);
        put(FIELD_DELIMITER, RECORD_BORDER, BETWEEN_FIELDS);
        put(OTHER, RECORD_BORDER, NON_QUOTED_FIELD);
        put(RECORD_DELIMITER, RECORD_BORDER, RECORD_BORDER);
        put(SPACE, RECORD_BORDER, SPACE_BETWEEN_FIELDS);

        put(RECORD_DELIMITER, BETWEEN_FIELDS, RECORD_BORDER);
        put(FIELD_DELIMITER, BETWEEN_FIELDS, BETWEEN_FIELDS);
        put(OTHER, BETWEEN_FIELDS, NON_QUOTED_FIELD);
        put(QUOTE, BETWEEN_FIELDS, QUOTE_BEGIN);
        put(SPACE, BETWEEN_FIELDS, SPACE_BETWEEN_FIELDS);

        put(RECORD_DELIMITER, SPACE_BETWEEN_FIELDS, RECORD_BORDER);
        put(FIELD_DELIMITER, SPACE_BETWEEN_FIELDS, BETWEEN_FIELDS);
        put(OTHER, SPACE_BETWEEN_FIELDS, NON_QUOTED_FIELD);
        put(QUOTE, SPACE_BETWEEN_FIELDS, QUOTE_BEGIN);
        put(SPACE, SPACE_BETWEEN_FIELDS, SPACE_BETWEEN_FIELDS);
    }
}
