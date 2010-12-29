package org.lf.util;

public class StringUtils {
    public static int[] prefixFinction(CharSequence charSequence) {
        int n = charSequence.length();
        int[] p = new int[n];
        for (int i = 1; i < n; ++i) {
            int k = p[i - 1];
            while(k > 0 && charSequence.charAt(k) != charSequence.charAt(i)) {
                k = p[k - 1];
            }
            p[i] = k + (charSequence.charAt(k) == charSequence.charAt(i) ? 1 : 0);
        }
        return p;
    }

    public static int indexOf(CharSequence where, CharSequence what, int[] prefixFunction) {
        int m = what.length();
        int n = where.length();
        if (n < m) {
            return -1;
        }
        if (m == 0) {
            return 0;
        }
        int k = 0;
        for (int i = 0; i < n; ++i) {
            while (k > 0 && where.charAt(i) != what.charAt(k)) {
                k = prefixFunction[k - 1];
            }
            if (where.charAt(i) == what.charAt(k)) {
                ++k;
            }
            if (k ==m) {
                return i - m + 1;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence where, CharSequence what) {
        return indexOf(where, what, prefixFinction(what));
    }
}
