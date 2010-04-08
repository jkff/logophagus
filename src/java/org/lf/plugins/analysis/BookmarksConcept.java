package org.lf.plugins.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.lf.logs.Log;
import org.lf.plugins.AttributeConcept;

public class BookmarksConcept implements AttributeConcept<BookmarksConcept, Bookmarks> {
    @Override
    public Bookmarks join(Collection<Bookmarks> parents, Log attributeOwner) {
        Bookmarks result = new Bookmarks(null, attributeOwner);
        for (Bookmarks b : parents) {
            List<String> names = b.getNames();
            for (String name : names) {
                try {
				    result.addBookmark(name, attributeOwner.convertToNative(b.getValue(name)));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
