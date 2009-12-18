package org.lf.parser.CSVParser;

import org.lf.parser.CSVParser.CSVParser.State;
import org.lf.parser.CSVParser.CSVParser.SymbolType;

class StateAndSymbolType{
	SymbolType symbol;
	State state;
	public StateAndSymbolType(State state, SymbolType symbol) {
		this.symbol = symbol;
		this.state = state;
	}
}
