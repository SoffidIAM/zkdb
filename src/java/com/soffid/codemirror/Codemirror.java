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

package com.soffid.codemirror;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.zkoss.xml.HTMLs;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.impl.InputElement;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.component.DataTextbox;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

/**
 * The component used to represent &lt;a
 * href="http://www.codepress.org/"&gt;Codepress Sourcecode Editor&tl;/a&gt;
 * 
 * Adaptació per Alejandro Usero Ruiz  - abril de 2011 :)
 * 
 * @author thomas mueller
 * @version $Revision: 1.1 $ $Date: 2011-05-11 10:25:21 $
 */
public class Codemirror extends InputElement implements XPathSubscriber {
	private static final long serialVersionUID = 1L;
	private SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate = false;

	private String style = "overflow: hidden";
	
	private String _language = "text";
	
	private boolean _linenumbers = true;
	private boolean _autocomplete = false;
	private String globalVars;
	
	public String getGlobalVars() {
		return globalVars;
	}

	public void setGlobalVars(String globalVars) {
		this.globalVars = globalVars;
	}

	public void setGlobalVars(Map<String, String> globalVars) {
		StringBuffer sb = new StringBuffer();
		for (String k: globalVars.keySet())
		{
			if (sb.length() > 0)
				sb.append(",");
			sb.append("'")
				.append(k.replaceAll("'", "\\'"))
				.append("':'")
				.append(globalVars.get(k).replaceAll("'", "\\'"))
				.append("'");
		}
		this.globalVars = sb.toString();
		invalidate();
	}

	private static final Map <String, String> languages = initLanguages();

	public Codemirror () {
	}
	
	public void onChange(InputEvent evt){
		setValue( evt.getValue() );
	}
	
	/**
	 * Sets the value for this codepress editor.
	 */
	public void setValue(String value) {
		if (value == null)
			value = "";
		if (!value.equals( getRawValue()  )) {
			setValueDirectly( value );
			smartUpdate("value", value);
			if (! duringOnUpdate )
			{
				binder.setValue(value);
			}
		}
	}
	
	@Override
	public void setText(String value) throws WrongValueException {
		super.setText(value);
		if (binder.isValid() && !binder.isVoid())
			binder.setValue(value);
	}

	public String getValue()
	{
		return coerceToString(getRawValue());
	}


	/**
	 * Returns the HTML attributes for this tag.
	 * <p>
	 * Used only for component development, not for application developers.
	 */
	public String getOuterAttrs() {
		StringBuffer sb = new StringBuffer();
		sb.append (Events.isListened(this, "onChange", true) ? " z.onChange=\"true\"": "");
		HTMLs.appendAttribute(sb, "z.lang", getLanguage());
		HTMLs.appendAttribute(sb, "z.readonly", isReadonly());
		HTMLs.appendAttribute(sb, "z.linenumbers", _linenumbers);
		HTMLs.appendAttribute(sb, "z.globalvars", globalVars);
		HTMLs.appendAttribute(sb, "style", getStyle());
		HTMLs.appendAttribute(sb, "width", getWidth());
		HTMLs.appendAttribute(sb, "height", getHeight());
		return sb.toString();
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
		if (! language.equals(_language))
		{
			_language = language;
			invalidate();
		}
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
		smartUpdate("z.linenumbers", linenumbers);
	}

	/**
	 * @param readonly the readonly to set
	 */
	public void setReadonly(boolean readonly) {
		if (readonly != isReadonly())
		{
			super.setReadonly(readonly);
			invalidate();
		}
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
		
		languages.put("html", "html");
		languages.put("java", "java");
		languages.put("javascript", "javascript");
		languages.put("perl", "perl");
		languages.put("php", "php");
		languages.put("ruby", "ruby");
		languages.put("sql", "sql");
		languages.put("txt", "text");
		languages.put("zul", "zul");
		languages.put("xml","xml");
		
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

	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#getBind()
	 */
	public String getBind() {		
		return binder.getDataPath();
		
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#getDataSource()
	 */
	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#setBind(java.lang.String)
	 */
	public void setBind(String bind) {
		binder.setDataPath(bind);
		if (bind != null)
		{
			refreshValue ();
		}
	}

	public void onUpdate (XPathEvent event) {
		refreshValue ();
	}

	private void refreshValue ()
	{
		Object newVal = binder.getValue();
		if (newVal == null)
			newVal = "";
		try {
			duringOnUpdate = true;
			setValue( newVal.toString() );
		} catch (WrongValueException e) {
			// Ignore
		} finally {
			duringOnUpdate = false;
		}
		
//		if (!binder.isVoid () && ! binder.isValid())
//			super.setReadonly(true);
//		else
//			super.setReadonly(false);
	}

	public Object clone() {
		Codemirror clone = (Codemirror) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}

	@Override
	public boolean setVisible(boolean visible) {
		boolean b = super.setVisible(visible);
		invalidate();
		return b;
	}
	
	public void refresh()
	{
	    response ("refresh", new AuInvoke (this, "refresh")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
