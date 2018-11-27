/**
 * 
 */
package es.caib.zkib.binder;

import java.util.Calendar;
import java.util.Date;


import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathException;
import es.caib.zkib.jxpath.JXPathTypeConversionException;
import es.caib.zkib.jxpath.Pointer;
import es.caib.zkib.jxpath.ri.model.beans.NullPointer;


public class SingletonBinder extends AbstractBinder {
		transient private Pointer _pointer;
		private String _xPath;
		transient private JXPathContext _xPathContext = null;
		private Object oldValue = null;
		
		public SingletonBinder (Component component)
		{
			super (component);
		}


		/**
		 * 
		 */
		protected void parsePath() {
			try {
				_xPathContext = null;
				if ( getXPath () != null && getDataSource() != null)
				{
					_pointer = getDataSource().getJXPathContext().getPointer(getXPath());
					_xPath = _pointer.asPath();
				}
			} catch (JXPathException e) {
				LogFactory.getLog(getClass()).debug("Error evaluating xpath " +
						getXPath() + ":" + e.toString());
				_pointer = new NullPointer (null, getDataSource().getJXPathContext().getLocale());
				_xPath = getXPath();
			}
		}
		
		public Pointer getPointer ()
		{
			smartParse();
			return _pointer;
		}
		
		public Object getValue ()
		{
			smartParse();
			if (getPointer() == null)
				return null;
			else
			{
				try {
					return getPointer().getValue();
				} catch (Exception e) {
					_pointer = null;
					invalidate();
					return null;
				}
			}
		}

		public Object getValue (Object defaultValue)
		{
			smartParse();
			Pointer p = getPointer ();
			if (p == null)
			{
				return defaultValue;
			}
			else
			{
				try {
					return p.getValue();
				} catch (Exception e) {
					LogFactory.getLog(getClass())
						.debug("Error getting pointer value for "+p.asPath(), e);
					_pointer = null;
					invalidate();
					return defaultValue;
				}
			}
		}

		public void setValue (Object value)
		{
			smartParse();
			if (getPointer() != null)
			{
				oldValue = getPointer().getValue();
				if ( oldValue == null && value != null ||
					 oldValue != null && !oldValue.equals(value) )
				{
					try {
						if (value != null && value instanceof DataNode)
							getPointer().setValue(((DataNode)value).getInstance());
						else
							getPointer().setValue(value);
					} catch (JXPathTypeConversionException e) {
						if (value instanceof Calendar)
							getPointer().setValue (((Calendar) value).getTime());
						else if (value instanceof Date)
						{
							Calendar c = Calendar.getInstance();
							c.setTime((Date)value);
							getPointer().setValue (c);
						}
					}
				}
			}
			else if (getDataPath() != null)
				throw new UiException ("Invalid xPath "+getDataPath());
		}

		public void setOldValue(){
			setValue(oldValue);
		}
		
		public JXPathContext getJXPathContext() {
			smartParse();
			if (_xPathContext == null && getDataSource() != null)
			{
				_xPathContext =  getDataSource().getJXPathContext().getRelativeContext(getPointer());
			}
			return _xPathContext;
		}
		
		protected String getXPathSubscription ()
		{
			return _xPath;
		}


		public boolean isValid() {
			return _pointer != null && ! (_pointer instanceof NullPointer);
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (getComponent() == null)
				return "NullBinder["+_xPath+"]";
			else
				return "Binder on "+getComponent().getDefinition().getName()+":"+getComponent().getId()+" ["+_xPath+"] ";
		}

}