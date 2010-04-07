package org.lf.services;

/**
 * User: jkff
 * Date: Nov 19, 2009
 * Time: 3:10:51 PM
 */
public class PluginException extends Exception {
    public PluginException() {
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }
}
