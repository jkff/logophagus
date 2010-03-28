package org.lf.plugins.analysis;

import java.io.IOException;
import java.util.*;

import org.lf.logs.Log;
import org.lf.parser.Position;
import org.lf.plugins.AttributeInstance;
import org.lf.util.CollectionFactory;

import static org.lf.util.CollectionFactory.newList;

public class Bookmarks implements AttributeInstance<BookmarksConcept,Bookmarks> {
    private final Bookmarks parent;
    private final Log log;
    
    private Map<String, Position> myBookmarks = CollectionFactory.newLinkedHashMap();

    public Bookmarks(Bookmarks parent, Log bookmarksOwner) {
        this.parent = parent;
        this.log = bookmarksOwner;
    }


    public List<String> getNames() {
        ArrayList<String> result = newList(myBookmarks.keySet());
        if (parent != null)
            result.addAll(parent.getNames());
        return result;
    }

    //positions from myBookmarks are not converted.
    //Caching for converted positions values 
    public Position getValue(String name) throws IOException {
        if (myBookmarks.containsKey(name))
            return myBookmarks.get(name);
        if (parent == null)
            return null;
        Position parentPos = parent.getValue(name);
        if (parentPos == null) return null;
        Position nativePos = log.convertToNative(parentPos);
        addBookmark(name, nativePos);
        return nativePos;
    }

    public int getSize() {
        return myBookmarks.size() + (parent == null ? 0 : parent.getSize());
    }

    //it is only possible to add bookmark whose position belongs to this.log 
    public boolean addBookmark(String name, Position pos) {
    	if (pos.getCorrespondingLog() != this.log) 
    		throw new IllegalArgumentException("Position must be from bookmark owner log");
        if (myBookmarks.containsKey(name))
            return false;
        myBookmarks.put(name, pos);
        return true;
    }

    @Override
    public BookmarksConcept getConcept() {
        return new BookmarksConcept();
    }

    @Override
    public Bookmarks createChild(Log attributeOwner) {
        return new Bookmarks(this, attributeOwner);
    }

}
