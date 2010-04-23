package org.lf.util;

public class Triple<A, B, C> {
    public final A first;
    public final B second;
    public final C third;

    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triple triple = (Triple) o;

        if (first != null ? !first.equals(triple.first) : triple.first != null) return false;
        if (second != null ? !second.equals(triple.second) : triple.second != null) return false;
        if (third != null ? !third.equals(triple.third) : triple.third != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}