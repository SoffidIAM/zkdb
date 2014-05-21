package es.caib.zkib.datamodel.xml.validation;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "es.caib.zkib.datamodel.xml.validation.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		try {
            return es.caib.zkib.lang.MessageFactory.getString(BUNDLE_NAME, key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
