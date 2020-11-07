/*
Created 1 Jul 2011 - Richard Morris
 */
package org.singsurf.singsurf;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to handle externalisation of error messages.
 * Uses a bundle name of "org.singsurf.singsurf.messages"
 * @see ResourceBundle
 * @since 3.5
 *
 */
public class SingSurfMessages {
    private static final String BUNDLE_NAME = "org.singsurf.singsurf.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = 
    		ResourceBundle.getBundle(BUNDLE_NAME);

    private SingSurfMessages() { /* empty */  }

    /**
     * Gets the message associated with a given string.
     * @param key key to message
     * @return the message
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    public static boolean containsKey(String key) {
    	return RESOURCE_BUNDLE.containsKey(key);
    }
    /**
     * Gets a message and uses that to format its arguments. 
     * @param key key to message which is a format string used by {@link MessageFormat#format(String, Object...)}
     * @param args set of arguments to be passed to the format
     * @return Message format applied to arguments
     */
    public static String format(String key,Object ... args) {
    	return MessageFormat.format(getString(key),args);
    }
}
