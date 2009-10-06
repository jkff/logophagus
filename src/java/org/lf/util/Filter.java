package org.lf.util;

/**
 * User: jkff
 * Date: Oct 6, 2009
 * Time: 10:35:41 AM
 */
public interface Filter<T> {
    boolean accepts(T t);
}
