package org.lf.plugins.extension;

import org.jetbrains.annotations.NotNull;
import org.lf.util.Removable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;
import static org.lf.util.CollectionFactory.newList;

public class ExtensionPointsManager {
    private final Map<ExtensionPointID, List<ExtensionPoint>> extIDToExtPoint = newHashMap();

    private final Map<ExtensionPointID, List> extIDToBufferedContents = newHashMap();

    public <T> void registerExtensionPoint(ExtensionPointID<T> extID, ExtensionPoint<T> extPoint) {
        if(!extIDToExtPoint.containsKey(extID)) {
            extIDToExtPoint.put(extID, new ArrayList<ExtensionPoint>());
        }
        extIDToExtPoint.get(extID).add(extPoint);
        if(extIDToBufferedContents.containsKey(extID)) {
            for(Object value : extIDToBufferedContents.get(extID)) {
                for(ExtensionPoint ep : extIDToExtPoint.get(extID)) {
                    ep.addExtension(value);
                }
            }
            extIDToBufferedContents.remove(extID);
        }
    }

    @NotNull
    public <T> ExtensionPoint<T> getExtensionPoint(final ExtensionPointID<T> extID) {
        final List<ExtensionPoint> pt = extIDToExtPoint.get(extID);
        if(pt == null) {
            return new ExtensionPoint<T>() {
                public Removable addExtension(final T extension) {
                    if(!extIDToBufferedContents.containsKey(extID)) {
                        extIDToBufferedContents.put(extID, new ArrayList());
                    }
                    extIDToBufferedContents.get(extID).add(extension);
                    return new Removable() {
                        public void remove() {
                            extIDToBufferedContents.get(extID).remove(extension);
                        }
                    };
                }
            };
        } else {
            return new ExtensionPoint<T>() {
                @Override
                public Removable addExtension(T extension) {
                    final List<Removable> remove = newList();
                    for(ExtensionPoint ep : pt) {
                        ep.addExtension(extension);
                    }
                    Collections.reverse(remove);
                    return new Removable() {
                        public void remove() {
                            for(Removable rem : remove) {
                                rem.remove();
                            }
                        }
                    };
                }
            };
        }
    }

}