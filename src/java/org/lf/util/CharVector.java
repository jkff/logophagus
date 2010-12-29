package org.lf.util;

import java.util.Arrays;

public class CharVector implements CharSequence {
    protected char buf[];
    protected int count;

    @Override
    public int length() {
        return count;
    }

    @Override
    public char charAt(int index) {
        return buf[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CharVector(Arrays.copyOfRange(buf, start, end));
    }

    public CharVector() {
        this(32);
    }

    public CharVector(char buf[]) {
        this.buf = Arrays.copyOf(buf, buf.length);
        count = buf.length;
    }

    public CharVector(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        buf = new char[size];
    }

    public void add(char c) {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, buf.length << 1);
        }
        buf[count] = c;
        count = newcount;
    }

    public void add(CharSequence c) {
        int newcount = count + c.length();
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        for (int i = 0; i < c.length(); ++i) {
            buf[count + i] = c.charAt(i);
        }
        count = newcount;
    }

    public void add(char c[], int off, int len) {
        if ((off < 0) || (off > c.length) || (len < 0) ||
                ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newcount = count + len;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(c, off, buf, count, len);
        count = newcount;
    }

    public void add(int index, CharSequence c) {
        int newcount = count + c.length();
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        for (int i = index + c.length(); i < newcount; ++i) {
            buf[i] = buf[i - c.length()];
        }
        for (int i = index; i < index + c.length(); ++i) {
            buf[i] = c.charAt(i - index);
        }
        count = newcount;
    }

    public void add(char c[]) {
        int newcount = count + c.length;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(c, 0, buf, count, c.length);
        count = newcount;
    }

    public void clear() {
        count = 0;
    }

    public String toString() {
        return new String(buf, 0, count);
    }

}
