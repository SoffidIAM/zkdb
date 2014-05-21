package es.caib.zkib.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.zkoss.util.Locales;

public class MessageFactory
{
	private static Class localesClass = null;
	static Map<String, Map<Locale, ResourceBundle>> bundles = new HashMap<String, Map<Locale,ResourceBundle>>();
	
	public static Locale getLocale ()
	{
		Locale l = Locales.getCurrent();
		
		if (l == null)
			return Locale.getDefault();
		else
			return l;
	}
	
	public static String getString(String bundleName, String message)
	{
    	ResourceBundle resources;
    	Locale l = getLocale ();
    
    	synchronized (bundles) {
    		Map<Locale, ResourceBundle> map = bundles.get(bundleName);
    		if (map == null)
    		{
    			map = new HashMap<Locale, ResourceBundle>();
    			bundles.put(bundleName, map);
    		}
    		
    		resources = map.get(l);
    		
    		if (resources == null)
    		{
    			resources = ResourceBundle.getBundle(bundleName, l, 
    					ResourceBundle.Control.getNoFallbackControl(
    							ResourceBundle.Control.FORMAT_PROPERTIES));
    			map.put(l, resources);
    		}
    	}
    	
    	return resources.getString(message);
    }
}
