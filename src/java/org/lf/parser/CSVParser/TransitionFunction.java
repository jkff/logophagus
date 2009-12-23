package org.lf.parser.CSVParser;

/**
 * User: jkff
 * Date: Dec 18, 2009
 * Time: 5:26:13 PM
 */
interface TransitionFunction<T, C> {
    T next(T cur, C in);
}
