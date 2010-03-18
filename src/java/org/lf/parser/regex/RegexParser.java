package org.lf.parser.regex;

import java.io.IOException;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lf.logs.Record;
import org.lf.parser.Parser;
import org.lf.parser.ScrollableInputStream;
import org.lf.parser.CharStream;
import org.lf.util.Pair;
import static org.lf.util.CollectionFactory.pair;
import static org.lf.util.CollectionFactory.newList;;


public class RegexParser implements Parser {
    private final Pattern pattern;

    public RegexParser(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public long findNextRecord(ScrollableInputStream is) throws IOException {
        return  matchInCharStream(forward(is), true).first;
    }

    @Override
    public long findPrevRecord(ScrollableInputStream is) throws IOException {
        if (is.scrollBack(1) == 0)
            return 0;
        return matchInCharStream(backward(is), false).first;
    }



    @Override
    public Record readRecord(ScrollableInputStream is) throws IOException {
        Pair<Long,MatchResult> matchRes =matchInCharStream(forward(is), true);
        return new MatcherRecord(matchRes.second);
    }

    private Pair<Long,MatchResult> matchInCharStream(CharStream cs, boolean isForward) throws IOException {
        StringBuilder record = new StringBuilder();
        long offset = 0;
        Matcher matcher;
        do {
            int c = cs.next();
            if (c == -1 ) return pair(0L, null);
            ++offset;
            if (isForward)     record.append((char)c);
            else             record.insert(0, (char)c);
            matcher = pattern.matcher(record);
        } while (!matcher.matches());

        MatchResult mayBeResult = matcher.toMatchResult();
        //continue greedy search
        while (true) {
            int c = cs.next();
            if (c == -1 ) break;
            if (isForward)     record.append((char)c);
            else             record.insert(0, (char)c);
            matcher = pattern.matcher(record);
            if (!matcher.matches()) break;
            mayBeResult = matcher.toMatchResult();
            ++offset;
        }

        return pair(offset, mayBeResult);
    }

    private CharStream forward(final ScrollableInputStream is) {
        return new CharStream() {
            public int next() throws IOException {
                return is.read();
            }
        };
    }

    private CharStream backward(final ScrollableInputStream is) {
        return new CharStream() {
            public int next() throws IOException {
                if (is.scrollBack(1) == 0)
                    return -1;
                int res = is.read();
                is.scrollBack(1);
                return res;
            }
        };
    }

    private class MatcherRecord implements Record {
        private final List<String> fields;

        private MatcherRecord(MatchResult matchResult) {
            fields = newList();

            //there should be minimum one group(one field)
            int fieldGroup = 1;
            fields.add(matchResult.group(fieldGroup));
            //zero group is not included in groupCount() result
            for(int i = 2; i <= matchResult.groupCount(); ++i) {
                if (matchResult.end(fieldGroup) < matchResult.start(i)) {
                    fields.add(matchResult.group(i));
                    fieldGroup = i;
                }
            }
        }

        public String get(int index) { return fields.get(index); }
        public int    size()         { return fields.size();     }
    }

}
