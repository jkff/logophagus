package org.lf.ui.components.plugins.scrollablelog;

import java.util.LinkedList;
import java.util.List;

class CyclicBuffer<T> {
    private final List<T> ts;
    private final int maxSize;

    CyclicBuffer(int maxSize) {
        this.ts = new LinkedList<T>();
        this.maxSize = maxSize;
    }

    void pushBegin(T t) {
        if (ts.size() == maxSize)
            ts.remove(maxSize-1);
        ts.add(0, t);
    }

    void pushEnd(T t) {
        if (ts.size() == maxSize)
            ts.remove(0);
        ts.add(t);
    }

    T get(int index) {
        return ts.get(index);
    }

    void clear() {
        ts.clear();
    }

    int size() {
        return ts.size();
    }
}
