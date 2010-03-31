package org.lf.util;

/**
 * Created on: 26.03.2010 21:40:15
 */
public interface ProgressListener<T> {
    boolean reportProgress(T progress);
}
