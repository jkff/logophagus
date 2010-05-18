package org.lf.plugins.extension;

import com.sun.istack.internal.Nullable;

import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;

public class ExtensionPointsManager {
    private final static Map<ExtensionPointID, ExtensionPoint> extIDToExtPoint = newHashMap();

    public static <T> void registerExtensionPoint(ExtensionPointID<T> extID, ExtensionPoint<T> extPoint) {
        extIDToExtPoint.put(extID, extPoint);
    }

    @Nullable
    public static <T> ExtensionPoint<T> getExtensionPoint(ExtensionPointID<T> extID) {
        return extIDToExtPoint.get(extID);
    }

}