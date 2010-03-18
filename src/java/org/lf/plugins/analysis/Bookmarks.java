package org.lf.plugins.analysis;

import java.util.*;

import org.lf.parser.Position;
import org.lf.plugins.AttributeConcept;
import org.lf.plugins.AttributeInstance;
import org.lf.util.CollectionFactory;

import static org.lf.util.CollectionFactory.newList;

public class Bookmarks implements AttributeInstance<BookmarksConcept,Bookmarks> {
    private Bookmarks parent;
    private Map<String, Position> data = CollectionFactory.newLinkedHashMap();

    public Bookmarks(Bookmarks parent) {
        this.parent = parent;
    }


    public List<String> getNames() {
        ArrayList<String> result = newList(data.keySet());
        if (parent != null)
            result.addAll(parent.getNames());
        return result;
    }

    public Position getValue(String name) {
        if (data.containsKey(name))
            return data.get(name);
        if (parent == null)
            return null;
        return parent.getValue(name);
    }

    public int getSize() {
        return data.size() + (parent == null ? 0 : parent.getSize());
    }

    public boolean addBookmark(String name, Position pos) {
        if (data.containsKey(name))
            return false;
        data.put(name, pos);
        return true;
    }

    @Override
    public BookmarksConcept getConcept() {
        return new BookmarksConcept();
    }

    @Override
    public Bookmarks createChild() {

        return new Bookmarks(this);
    }


}
