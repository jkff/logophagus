package org.lf.logs;

import java.io.UnsupportedEncodingException;

/**
 * Created on: 01.06.2010 23:50:04
 */
public class LineRecord implements Record {
    private CharSequence value;

    public LineRecord(String value) {
        this.value = value;
    }

    public LineRecord(final byte[] buf, final int offset, final int len) {
        this.value = new CharSequence() {
            @Override
            public int length() {
                return len;
            }

            @Override
            public char charAt(int index) {
                return (char)buf[offset+index];
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new LineRecord(buf, offset+start, end-start).getCell(0);
            }

            public String toString() {
                try {
                    return new String(buf, offset, len, "us-ascii");
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError();
                }
            }
        };
    }

    @Override
    public int getCellCount() {
        return 1;
    }

    @Override
    public CharSequence getCell(int i) {
        if(i != 0)
            throw new IndexOutOfBoundsException("Cell index too large: " + i + " > 0");
        return value;
    }

    @Override
    public Format getFormat() {
        return Format.UNKNOWN_FORMAT;
    }
}
