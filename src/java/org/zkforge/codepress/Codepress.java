/** 
 * ZK port of CodePress - Real Time Syntax Highlighting Editor written in JavaScript - http://codepress.org/
 * 
 * Copyright (C) 2008 Thomas Mueller <thomas.mueller@empego.net>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation.
 * 
 * Read the full licence: http://www.opensource.org/licenses/lgpl-license.php
 */

package org.zkforge.codepress;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.impl.InputElement;

/**
 * The component used to represent &lt;a
 * href="http://www.codepress.org/"&gt;Codepress Sourcecode Editor&tl;/a&gt;
 * 
 * Adaptació per Alejandro Usero Ruiz  - abril de 2011 :)
 * 
 * @author thomas mueller
 * @version $Revision: 1.1 $ $Date: 2011-05-11 10:25:21 $
 */
public class Codepress extends InputElement {
	private static final long serialVersionUID = 1L;

	private String style = "";
	
	private String _value = "";

	private String _language = "text";
	
	private boolean _linenumbers = true;
	private boolean _readonly = false;
	private boolean _autocomplete = false;
	
	private static final Map <String, String> languages = initLanguages();
	
	
	public void onChange(InputEvent evt){
		this._value = evt.getValue();
	}
	
	
	/**
	 * Returns the value in this codepress editor.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * Sets the value for this codepress editor.
	 */
	public void setValue(String value) {
		if (value == null)
			value = "";
		if (!value.equals(_value)) {
			_value = value;
			smartUpdate("value", value);
		}
	}


	/**
	 * Returns the HTML attributes for this tag.
	 * <p>
	 * Used only for component development, not for application developers.
	 */
	public String getOuterAttrs() {
		return Events.isListened(this, "onChange", true) ? " z.onChange=\"true\""
				: null;
	}

	// -- Super --//
	/** Not childable. */
	public boolean isChildable() {
		return false;
	}


	/**
	 * @return the _language
	 */
	public String getLanguage() {
		return _language;
	}

	/**
	 * @param language the _language to set
	 */
	public void setLanguage(String language) {
		if(!this.getLanguages().contains(language)){
			throw new RuntimeException("Language '" + language + "' not supported");
		}
		_language = language;
	}

	/**
	 * @return the linenumbers
	 */
	public String getLinenumbers() {
		return _linenumbers? "": "linenumbers-off";
	}

	/**
	 * @param linenumbers the linenumbers to set
	 */
	public void setLinenumbers(boolean linenumbers) {
		_linenumbers = linenumbers;
	}

	/**
	 * @return the readonly
	 */
	public String getReadonly() {
		return _readonly? "readonly-on":"";
	}

	/**
	 * @param readonly the readonly to set
	 */
	public void setReadonly(boolean readonly) {
		_readonly = readonly;
	}

	/**
	 * @return the autocomplete
	 */
	public String getAutocomplete() {
		return _autocomplete? "":"autocomplete-off";
	}
	
	/**
	 * @param autocomplete the autocomplete to set
	 */
	public void setAutocomplete(boolean autocomplete) {
		_autocomplete = autocomplete;
	}
	
	public String getOptions(){
		return "codepress " + getLanguage() + " " + getReadonly() + " " + getAutocomplete() + " " +getLinenumbers();
	}

	/**
	 * @return the style
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * @param style the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * Select the language by the file extension
	 * @param extension
	 */
	public void setLanguageExtension(String extension){
		String language = languages.get(extension);
		if(language == null){
			throw new RuntimeException("Extension '" + extension + "' not supported");
		}
		
		this.setLanguage(language);
	}
	
	/**
	 * initialize a mapping from file extensions to language names supported by codepress
	 * @return
	 */
	private static Map initLanguages(){
		//a sorted map of supported languages mapped to corresponding file extensions 
		TreeMap <String, String> languages = new TreeMap <String, String> ();
		
		languages.put("cs", "csharp");
		languages.put("css", "css");
		languages.put("html", "html");
		languages.put("java", "java");
		languages.put("js", "javascript");
		languages.put("perl", "perl");
		languages.put("php", "php");
		languages.put("ruby", "ruby");
		languages.put("sql", "sql");
		languages.put("txt", "text");
		languages.put("vb", "vbscript");
		languages.put("zul", "zul");
		languages.put("mazinger","mazinger");
		
		return languages;
	}
	
	/**
	 * get a collection of supported language names. Useful to fill language select listbox
	 * @return a Collection of supported languages
	 */
	public static final Collection <String>  getLanguages(){
		return languages.values();
	}
	
	/**
	 * get a Set of supported language extensions. Useful to fill language select listbox
	 * @return a Collection of supported languages
	 */
	public static final Set <String> getLanguageExtensions(){
		return languages.keySet();
	}


	//-- super --//
	/** Coerces the value passed to {@link #setValue}.
	 *
	 * <p>Default: convert null to an empty string.
	 */
	protected Object coerceFromString(String value) throws WrongValueException {
		return value != null ? value: "";
	}
	/** Coerces the value passed to {@link #setValue}.
	 *
	 * <p>Default: convert null to an empty string.
	 */
	protected String coerceToString(Object value) {
		return value != null ? (String)value: "";
	}

}
