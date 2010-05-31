package org.lf.io.zlib;

import java.util.List;

/**
 * Created on: 28.05.2010 18:08:11
 */
public class IndexMemento {
    public final List<ZRan.Point> idx;
    public final long decompressedSize;

    public IndexMemento(List<ZRan.Point> idx, long decompressedSize) {
        this.idx = idx;
        this.decompressedSize = decompressedSize;
    }
}
