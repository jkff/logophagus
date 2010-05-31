package org.lf.util;

import java.util.*;

public class CollectionFactory {
    public static <T> ArrayList<T> newList() {
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> newList(Collection<T> ts) {
        return new ArrayList<T>(ts);
    }

    public static <T> LinkedList<T> newLinkedList() {
        return new LinkedList<T>();
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    public static <K> HashSet<K> newHashSet() {
        return new HashSet<K>();
    }

    public static <T> PriorityQueue<T> newPriorityQueue() {
        return new PriorityQueue<T>();
    }

    public static <T> PriorityQueue<T> newPriorityQueue(PriorityQueue<T> other) {
        return new PriorityQueue<T>(other);
    }

    public static <A, B> Pair<A, B> pair(A a, B b) {
        return new Pair<A, B>(a, b);
    }

    public static <A, B, C> Triple<A, B, C> triple(A a, B b, C c) {
        return new Triple<A, B, C>(a, b, c);
    }

}
