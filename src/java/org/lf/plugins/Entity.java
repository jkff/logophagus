package org.lf.plugins;

/**
 * User: jkff
 * Date: Jan 26, 2010
 * Time: 3:45:07 PM
 */
public class Entity {
    public final Attributes attributes;
    public final Object data;

    public Entity(Attributes attributes, Object data) {
        this.attributes = attributes;
        this.data = data;
    }
}
