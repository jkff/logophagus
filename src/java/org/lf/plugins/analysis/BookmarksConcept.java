package org.lf.plugins.analysis;

import org.lf.logs.Log;
import org.lf.plugins.AttributeConcept;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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
                    System.out.println("Can't convert bookmark with name" + name
                            + "from " + b.getLog().toString() + " log, to bookmark of"
                            + attributeOwner.toString() + " log");
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
