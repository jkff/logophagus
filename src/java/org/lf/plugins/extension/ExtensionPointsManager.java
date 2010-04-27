package org.lf.plugins.extension;

import com.sun.istack.internal.Nullable;

import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;

public class ExtensionPointsManager {
    private final static Map<ExtensionID, ExtensionPoint> extIDToExtPoint = newHashMap();

    public static void registerExtensionPoint(ExtensionID extID, ExtensionPoint extPoint) {
        extIDToExtPoint.put(extID, extPoint);
    }

    @Nullable
    public static ExtensionPoint getExtensionPoint(ExtensionID extID) {
        return extIDToExtPoint.get(extID);
    }
}