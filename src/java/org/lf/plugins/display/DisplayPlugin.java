package org.lf.plugins.display;

import org.lf.plugins.Entity;

import javax.swing.*;

public interface DisplayPlugin {
    boolean isApplicableFor(Object o);

    View createView(Entity entity);
}