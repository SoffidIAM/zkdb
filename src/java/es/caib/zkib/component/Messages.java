package es.caib.zkib.component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.zkoss.util.Locales;

public class Messages {
	private static final String BUNDLE_NAME = "es.caib.zkib.component.messages"; //$NON-NLS-1$

	private static Class localesClass = null;
	private static Method getCurrentMethod = null;
	
	static Map<Locale, ResourceBundle> bundles = new Hashtable<Locale,ResourceBundle>();

    public static String getString(String message) {
    	ResourceBundle resources;
    	Locale l = Locales.getCurrent();
    	synchronized (bundles) {
    		resources = bundles.get(l);
    		if (resources == null) {
    			try {
        			resources = ResourceBundle.getBundle(BUNDLE_NAME, l,
            				ResourceBundle.Control.getNoFallbackControl(
            					ResourceBundle.Control.FORMAT_PROPERTIES));

    			} catch (MissingResourceException e)
    			{
        			resources = ResourceBundle.getBundle(BUNDLE_NAME, l,
        					Thread.currentThread().getContextClassLoader(),
            				ResourceBundle.Control.getNoFallbackControl(
            					ResourceBundle.Control.FORMAT_PROPERTIES));

    			}
    			bundles.put(l, resources);
    		}
    	}
    	return resources.getString(message);
    }
}
