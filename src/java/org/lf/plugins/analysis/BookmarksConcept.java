package org.lf.plugins.analysis;

import java.util.List;

import org.lf.plugins.AttributeConcept;
import org.lf.plugins.AttributeInstance;

public class BookmarksConcept implements AttributeConcept<BookmarksConcept> {
    @Override
    public AttributeInstance<BookmarksConcept> join(AttributeInstance<BookmarksConcept>[] children) {
        Bookmarks result = new Bookmarks(null);
        for (AttributeInstance<BookmarksConcept> attributeInstance : children) {
            List<String> names = ((Bookmarks)attributeInstance).getNames();
            for (String name : names) {
                result.addBookmark(name, ((Bookmarks)attributeInstance).getValue(name));
            }
        }
        return result;
    }
}
