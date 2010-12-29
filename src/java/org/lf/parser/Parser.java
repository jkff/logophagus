package org.lf.parser;

import org.lf.encoding.ScrollableReader;
import org.lf.io.ScrollableInputStream;
import org.lf.logs.Format;
import org.lf.logs.Record;

import java.io.IOException;

public interface Parser {
    Record readRecord(ScrollableReader reader) throws IOException;

    /**
     *
     * @param reader
     * @return offset of next record in reader or -1 if there is no prev records
     * @throws IOException
     */
    long findNextRecord(ScrollableReader reader) throws IOException;

    /**
     *
     *
     * @param reader
     * @return offset of prev record in reader or -1 if there is no prev records
     * @throws IOException
     */
    long findPrevRecord(ScrollableReader reader) throws IOException;

    /**
     * @return formats of records that readRecord() can return
     *         if your parser can't match record with it's predefined formats then return record with Format.UnknownFormat.
     *         You shouldn't include format of {@link Format#UNKNOWN_FORMAT} to getFormats() result
     */
    Format[] getFormats();
}
