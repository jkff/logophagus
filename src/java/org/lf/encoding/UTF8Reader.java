package org.lf.encoding;

import org.lf.io.ScrollableInputStream;
import org.lf.util.CharVector;
import org.lf.util.ReverseCharVector;

import java.io.IOException;

public class UTF8Reader implements ScrollableReader {
    private final ScrollableInputStream sis;
    private final static char UNKNOWN_SYMBOL = '?';

    public UTF8Reader(ScrollableInputStream sis) {
        this.sis = sis;
    }

    @Override
    public int next() throws IOException {
        int b0 = sis.next();
        if (b0 == -1) {
            return -1;
        }
        // UTF-8:   [0xxx xxxx]
        // Unicode: [0000 0000] [0xxx xxxx]
        if ((b0 & 0x80) == 0) {
            return (char)b0;
        }
        // UTF-8:   [110y yyyy] [10xx xxxx]
        // Unicode: [0000 0yyy] [yyxx xxxx]
        if ((b0 & 0xE0) == 0xC0) {
            int b1 = sis.next();
            validateNotBorder(b1);
            return (char)(((b0 << 6) & 0x07C0) | (b1 & 0x003F));
        }
        // UTF-8:   [1110 zzzz] [10yy yyyy] [10xx xxxx]
        // Unicode: [zzzz yyyy] [yyxx xxxx]
        if ((b0 & 0xF0) == 0xE0) {
            int b1 = sis.next();
            validateNotBorder(b1);
            int b2 = sis.next();
            validateNotBorder(b2);
            return  (char)(((b0 << 12) & 0xF000) | ((b1 << 6) & 0x0FC0) | (b2 & 0x003F));
        }

        sis.next();
        sis.next();
        sis.next();
        return UNKNOWN_SYMBOL;
    }

    @Override
    public int prev() throws IOException {
        int b0 = sis.prev();
        if (b0 == -1) {
            return -1;
        }
        // UTF-8:   [0xxx xxxx]
        // Unicode: [0000 0000] [0xxx xxxx]
        if ((b0 & 0x80) == 0) {
            return (char)b0;
        }
        // UTF-8:   [110y yyyy] [10xx xxxx]
        // Unicode: [0000 0yyy] [yyxx xxxx]
        int b1 = sis.prev();
        validateNotBorder(b1);
        if ((b1 & 0xE0) == 0xC0) {
            return (char)((b1 << 6) & 0x07C0) | (b0 & 0x003F);
        }
        // UTF-8:   [1110 zzzz] [10yy yyyy] [10xx xxxx]
        // Unicode: [zzzz yyyy] [yyxx xxxx]
        int b2 = sis.prev();
        validateNotBorder(b2);
        if ((b2 & 0xF0) == 0xE0) {
            return  (char)(((b2 << 12) & 0xF000) | ((b1 << 6) & 0x0FC0) | (b0 & 0x003F));
        }

        sis.next();
        return UNKNOWN_SYMBOL;
    }

    @Override
    public void scrollToBegin() throws IOException{
        sis.scrollTo(0);
    }

    @Override
    public void scrollToEnd() throws IOException {
        sis.scrollTo(sis.getMaxOffset());
    }

    private byte[] convertToUTF(char c) {
        // Unicode: [0000 0000] [0xxx xxxx]
        // UTF-8:   [0xxx xxxx]
        if ((c >> 8) == 0 && (c & 0x80) == 0) {
            return new byte[] {(byte)c};
        }
        // Unicode: [0000 0yyy] [yyxx xxxx]
        // UTF-8:   [110y yyyy] [10xx xxxx]
        if (((c >> 8) & 0xF8) == 0) {
            return new byte[] {(byte)(0xC0 | (c >> 6)), (byte)(0x80 | (c & 0x3f))};
        }
        // Unicode: [zzzz yyyy] [yyxx xxxx]
        // UTF-8:   [1110 zzzz] [10yy yyyy] [10xx xxxx]
        return new byte[] {(byte)(0xE0 | (c >> 12)), (byte)(0x80 | ((c >> 6) & 0x3f)), (byte)(0x80 | (c & 0x3f))};
    }

    @Override
    public boolean scrollForwardUntil(char c) throws IOException {
        byte[] bytes = convertToUTF(c);
        byte[] readed = sis.readForwardUntil(bytes[bytes.length - 1]);
        while(readed.length > 0 && readed[readed.length - 1] == bytes[bytes.length - 1]) {
            if (readed.length >= bytes.length) {
                boolean containsChar = true;
                for (int i = 1 ; i < bytes.length ; ++i) {
                    if (readed[readed.length - bytes.length + i] != bytes[i]) {
                        containsChar = false;
                        break;
                    }
                }
                if (containsChar) {
                    return true;
                }

            }
            readed = sis.readForwardUntil(bytes[bytes.length - 1]);
        }
        return false;
    }

    @Override
    public boolean scrollBackwardUntil(char c) throws IOException {
        byte[] bytes = convertToUTF(c);
        byte[] readed = sis.readBackwardUntil(bytes[0]);
        while(readed.length > 0 && readed[0] == bytes[0]) {
            if (readed.length >= bytes.length) {
                boolean containsChar = true;
                for (int i = 1 ; i < bytes.length ; ++i) {
                    if (readed[i] != bytes[i]) {
                        containsChar = false;
                        break;
                    }
                }
                if (containsChar) {
                    return true;
                }
            }
            readed = sis.readBackwardUntil(bytes[0]);
        }
        return false;
    }

    @Override
    public CharSequence readForwardUntil(char c) throws IOException {
        CharVector charVector = new CharVector();
        int cur = next();
        while(cur != -1 && cur != c) {
            charVector.add((char)cur);
            cur = next();
        }
        if (cur == c) {
            charVector.add(c);
        }
        return charVector;
    }

    @Override
    public CharSequence readBackwardUntil(char c) throws IOException {
        CharVector charVector = new ReverseCharVector();
        int cur = prev();
        while(cur != -1 && cur != c) {
            charVector.add((char)cur);
            cur = prev();
        }
        if (cur == c) {
            charVector.add(c);
        }
        return charVector;
    }

    @Override
    public boolean isSameSource(ScrollableReader scrollableReader) {
        return scrollableReader instanceof UTF8Reader && ((UTF8Reader)scrollableReader).sis.isSameSource(this.sis);
    }

    @Override
    public void scrollToOffset(long offset) throws IOException {
        this.sis.scrollTo(offset);
    }

    @Override
    public long getCurrentOffset() {
        return sis.getOffset();
    }

    private void validateNotBorder(int b) {
        if (b == -1) {
            throw new IllegalStateException("Unexpected file border");
        }
    }


}
