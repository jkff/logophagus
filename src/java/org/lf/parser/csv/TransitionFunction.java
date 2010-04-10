package org.lf.parser.csv;

interface TransitionFunction<T, C> {
    T next(T cur, C in);
}
