package org.lf.plugins.analysis;

import java.io.IOException;
import java.util.*;

import org.lf.logs.Log;
import org.lf.parser.Position;
import org.lf.plugins.AttributeInstance;
import org.lf.util.CollectionFactory;

import static org.lf.util.CollectionFactory.newHashSet;;

public class Bookmarks implements AttributeInstance<BookmarksConcept,Bookmarks> {
    private final Bookmarks parent;
    private final Log log;
    
    private Map<String, Position> name2pos = CollectionFactory.newLinkedHashMap();

    public Bookmarks(Bookmarks parent, Log bookmarksOwner) {
        this.parent = parent;
        this.log = bookmarksOwner;
    }

    public List<String> getNames() {
        Set<String> result = newHashSet();
        result.addAll(name2pos.keySet());
        if (parent != null)
            result.addAll(parent.getNames());
        return Arrays.asList(result.toArray(new String[0]));
    }

    //positions from name2pos are not converted.
    //Caching for converted positions 
    public Position getValue(String name) throws IOException {
        if (name2pos.containsKey(name))
            return name2pos.get(name);
        if (parent == null)
            return null;
        Position parentPos = parent.getValue(name);
        if (parentPos == null)
            return null;
        Position nativePos = log.convertToNative(parentPos);
        name2pos.put(name, nativePos);
        return nativePos;
    }

    public int getSize() {
        return getNames().size();
    }

    //it is only possible to add bookmark whose position belongs to this.log 
    public boolean addBookmark(String name, Position pos) {
        if (pos.getCorrespondingLog() != this.log) 
            throw new IllegalArgumentException("Position("+pos.getClass()+") must be from bookmark owner log("+log.getClass()+ ")");
        if (getNames().contains(name))
            return false;
        name2pos.put(name, pos);
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
