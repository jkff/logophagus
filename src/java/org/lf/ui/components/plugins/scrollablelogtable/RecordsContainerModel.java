package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.LinkedList;
import java.util.List;


class CyclicBuffer<T> {
    private final List<T> buffer;
    private final int maxSize;

    CyclicBuffer(int maxSize) {
        this.buffer = new LinkedList<T>();
        this.maxSize = maxSize;
    }

    void pushBegin(T element) {
        if (buffer.size() == maxSize)
            buffer.remove(maxSize-1);
        buffer.add(0, element);
    }

    void pushEnd(T pair) {
        if (buffer.size() == maxSize)
            buffer.remove(0);
        buffer.add(pair);
    }

    T get(int index) {
        return buffer.get(index);
    }

    void clear() {
        buffer.clear();
    }

    int maxSize() {
        return maxSize;
    }

    int size() {
        return buffer.size();
    }

}
