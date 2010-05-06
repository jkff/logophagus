package org.lf.util;

import java.util.Comparator;

public class Comparators {
    public static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static <T extends Comparable<? super T>> Comparator<T> inverse(final Comparator<T> other) {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return -1 * other.compare(o1, o2);
            }
        };
    }

}
