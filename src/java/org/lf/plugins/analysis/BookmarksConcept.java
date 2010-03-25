package org.lf.plugins.analysis;

import java.util.Collection;
import java.util.List;

import org.lf.plugins.AttributeConcept;

public class BookmarksConcept implements AttributeConcept<BookmarksConcept, Bookmarks> {
    @Override
    public Bookmarks join(Collection<Bookmarks> parents) {
        Bookmarks result = new Bookmarks(null);
        for (Bookmarks b : parents) {
            List<String> names = b.getNames();
            for (String name : names) {
                result.addBookmark(name, b.getValue(name));
            }
        }
        return result;
    }
}
