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

package com.soffid.ckeditor;

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
public class Ckeditor extends InputElement implements XPathSubscriber {
	private static final long serialVersionUID = 1L;
	private SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate = false;

	private String style = "overflow: hidden";
	
	public Ckeditor () {
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
		HTMLs.appendAttribute(sb, "z.readonly", isReadonly());
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
		
	}

	public Object clone() {
		Ckeditor clone = (Ckeditor) super.clone();
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
