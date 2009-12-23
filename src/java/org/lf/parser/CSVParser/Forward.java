package org.lf.parser.CSVParser;

import java.util.Comparator;
import java.util.TreeMap;

import org.lf.parser.CSVParser.State;
import org.lf.parser.CSVParser.SymbolType;

class Forward implements TransitionFunction<State, SymbolType> {
    // TODO Change like Backward

	public Forward()
	{

		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.QUOTE), State.QUOTE_END);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.ESCAPE), State.IN_QUOTE_ESCAPE);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.FIELD_DELIMITER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.RECORD_DELIMITER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.OTHER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.QUOTE), State.QUOTE_END); 
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.ESCAPE), State.IN_QUOTE_ESCAPE); 
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.FIELD_DELIMITER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.OTHER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.RECORD_DELIMITER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.QUOTE), State.QUOTE_END); 
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.ESCAPE), State.IN_QUOTE_ESCAPE);
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.FIELD_DELIMITER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.OTHER), State.IN_QUOTE); 
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.RECORD_DELIMITER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.QUOTE), State.DOUBLE_QUOTE);
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.ESCAPE), State.ERROR);
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.FIELD_DELIMITER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.OTHER), State.FIELD);
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.RECORD_DELIMITER), State.RECORD_BORDER);

		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.QUOTE), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.ESCAPE), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.FIELD_DELIMITER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.OTHER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.RECORD_DELIMITER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.RECORD_DELIMITER), State.RECORD_BORDER);
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.ESCAPE), State.FIELD);
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.FIELD_DELIMITER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.OTHER), State.FIELD);
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.QUOTE), State.QUOTE_BEGIN);

		map.put(new StateAndSymbolType(State.FIELD, SymbolType.QUOTE), State.QUOTE_BEGIN);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.ESCAPE), State.FIELD);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.FIELD_DELIMITER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.OTHER), State.FIELD);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.RECORD_DELIMITER), State.RECORD_BORDER);

		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.QUOTE), State.QUOTE_BEGIN);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.ESCAPE), State.FIELD);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.FIELD_DELIMITER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.OTHER), State.FIELD);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.RECORD_DELIMITER), State.RECORD_BORDER);

	}
}
