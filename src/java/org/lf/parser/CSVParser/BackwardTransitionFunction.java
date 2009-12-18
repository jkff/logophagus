package org.lf.parser.CSVParser;

import java.util.Comparator;
import java.util.TreeMap;

import org.lf.parser.CSVParser.CSVParser.State;
import org.lf.parser.CSVParser.CSVParser.SymbolType;

class BackwardTransitionFunction{
	private char recordDelimeter; 
	private char fieldDelimeter; 
	private char quoteCharacter; 
	private char escapeCharacter; 

	public BackwardTransitionFunction(char recordDelimeter, char fieldDelimeter,
			char quoteCharacter, char escapeCharacter)
	{

		this.recordDelimeter = recordDelimeter;
		this.fieldDelimeter = fieldDelimeter;
		this.quoteCharacter = quoteCharacter;
		this.escapeCharacter = escapeCharacter;
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.QUOTE), State.QUOTE_BEGIN);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.ESCAPE), State.IN_QUOTE_ESCAPE); 
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.FIELD_DELIMETER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.RECORD_DELIMETER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE, SymbolType.OTHER), State.IN_QUOTE); 

		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.QUOTE), State.QUOTE_BEGIN); 
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.ESCAPE), State.IN_QUOTE); 
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.FIELD_DELIMETER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.OTHER), State.IN_QUOTE); 
		map.put(new StateAndSymbolType(State.DOUBLE_QUOTE, SymbolType.RECORD_DELIMETER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.QUOTE), State.DOUBLE_QUOTE); 
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.ESCAPE), State.IN_QUOTE_ESCAPE); 
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.FIELD_DELIMETER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.OTHER), State.FIELD); 
		map.put(new StateAndSymbolType(State.QUOTE_BEGIN, SymbolType.RECORD_DELIMETER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.QUOTE), State.DOUBLE_QUOTE); 
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.ESCAPE), State.ERROR); 
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.FIELD_DELIMETER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.OTHER), State.FIELD); 
		map.put(new StateAndSymbolType(State.QUOTE_END, SymbolType.RECORD_DELIMETER), State.RECORD_BORDER);

		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.QUOTE), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.ESCAPE), State.IN_QUOTE); 
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.FIELD_DELIMETER), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.OTHER), State.IN_QUOTE); 
		map.put(new StateAndSymbolType(State.IN_QUOTE_ESCAPE, SymbolType.RECORD_DELIMETER), State.IN_QUOTE);

		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.RECORD_DELIMETER), State.ERROR); // \n,
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.ESCAPE), State.ERROR); //  \,abcd - all escapes only in quote
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.FIELD_DELIMETER), State.BETWEEN_FIELDS); //,, - empty field
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.OTHER), State.FIELD); // abc,
		map.put(new StateAndSymbolType(State.BETWEEN_FIELDS, SymbolType.QUOTE), State.IN_QUOTE);// abc",

		map.put(new StateAndSymbolType(State.FIELD, SymbolType.QUOTE), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.ESCAPE), State.ERROR);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.FIELD_DELIMETER), State.BETWEEN_FIELDS);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.OTHER), State.FIELD);
		map.put(new StateAndSymbolType(State.FIELD, SymbolType.RECORD_DELIMETER), State.RECORD_BORDER);

		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.QUOTE), State.IN_QUOTE);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.ESCAPE), State.ERROR);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.FIELD_DELIMETER), State.ERROR);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.OTHER), State.FIELD);
		map.put(new StateAndSymbolType(State.RECORD_BORDER, SymbolType.RECORD_DELIMETER), State.RECORD_BORDER);

	}


	private TreeMap<StateAndSymbolType, State> map = new TreeMap<StateAndSymbolType, State>(new Comparator<StateAndSymbolType>() {
		public int compare(StateAndSymbolType arg0, StateAndSymbolType arg1) {
			int numb0 = arg0.state.ordinal() + arg0.symbol.ordinal()*State.values().length;
			int numb1 = arg1.state.ordinal() + arg1.symbol.ordinal()*State.values().length;
			if (numb0 > numb1) 
				return 1;
			if (numb0 < numb1) 
				return -1;
			return 0;
		}
	});



	public State get(State s, char ch){
		if (recordDelimeter == ch)
			return map.get(new StateAndSymbolType(s, SymbolType.RECORD_DELIMETER));

		if (escapeCharacter == ch)
			return map.get(new StateAndSymbolType(s, SymbolType.ESCAPE));

		if (fieldDelimeter == ch) 
			return map.get(new StateAndSymbolType(s, SymbolType.FIELD_DELIMETER));

		if (quoteCharacter == ch) 
			return map.get(new StateAndSymbolType(s, SymbolType.QUOTE));

		return map.get(new StateAndSymbolType(s, SymbolType.OTHER));
	}	
}
