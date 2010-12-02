package org.lf.logs;

import java.util.regex.Matcher;

public class RegexRecord implements Record {
    private final Matcher matcher;
    private final Format format;

    public RegexRecord(Matcher matcher, Format format) {
        this.matcher = matcher;
        this.format = format;
    }

    @Override
    public int getCellCount() {
        return this.matcher.groupCount();
    }

    @Override
    public String getCell(int i) {
        return this.matcher.group(i+1);
    }

    @Override
    public Format getFormat() {
        return this.format;
    }

    @Override
    public CharSequence getRawString() {
        return matcher.group(0);
    }
}
