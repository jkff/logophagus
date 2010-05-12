package org.lf.plugins.extension;

import com.sun.istack.internal.Nullable;

import java.util.List;
import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;
import static org.lf.util.CollectionFactory.newList;

public class ExtensionPointsManager {
    private final static Map<ExtensionPointID, ExtensionPoint> extIDToExtPoint = newHashMap();
    private final static List<ExtensionTracer> listeners = newList();

    public static <T> void registerExtensionPoint(ExtensionPointID<T> extID, ExtensionPoint<T> extPoint) {
        extIDToExtPoint.put(extID, extPoint);
        for (ExtensionTracer cur : listeners) {
            cur.extensionAdd(extID);
        }
    }

    @Nullable
    public static <T> ExtensionPoint<T> getExtensionPoint(ExtensionPointID<T> extID) {
        return extIDToExtPoint.get(extID);
    }

    public static void addExtensionTracer(ExtensionTracer tracer) {
        listeners.add(tracer);
    }

    public static void removeExtensionTracer(ExtensionTracer tracer) {
        listeners.remove(tracer);
    }

}