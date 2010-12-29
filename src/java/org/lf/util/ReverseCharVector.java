package org.lf.util;

import java.util.Arrays;

public class ReverseCharVector extends CharVector {
    @Override
    public char charAt(int index) {
        return buf[count - 1 - index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new ReverseCharVector(Arrays.copyOfRange(buf, start, end));
    }

    public ReverseCharVector() {
        super();
    }

    public ReverseCharVector(int size) {
        super(size);
    }

    public ReverseCharVector(char buf[]) {
        super(buf);
    }

    @Override
    public String toString() {
        char result[] = new char[count];
        for (int i = 0; i < count; ++i) {
            result[count - 1 - i] = buf[i];
        }
        return new String(result);
    }

}
