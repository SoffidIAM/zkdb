/**
 * 
 */
package es.caib.zkib.binder;

import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import es.caib.zkib.component.MasterListItem;
import es.caib.zkib.component.MasterRow;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.Pointer;

public abstract class AbstractBinder implements BindContext, XPathSubscriber {
		/**
		 * 
		 */
		private DataSource _ds;
		private org.zkoss.zk.ui.Component _component;
		private String _bind;
		private String _xPath;
		List _subscribers = new Vector();
		private boolean _pendingParse = false;
		private boolean _processingXPathRerunEvent = false;
		
		protected void smartParse ()
		{
			if (_pendingParse && _bind != null)
			{
				_pendingParse = false;
				doParse ();
				if (!_pendingParse && ! _processingXPathRerunEvent)
					onUpdate ( new XPathRerunEvent (_ds, _xPath));
			}
		}
		
		public AbstractBinder (Component component)
		{
			_component = component;
			if ( _component instanceof XPathSubscriber)
				addSubscriber((XPathSubscriber) _component);
			_component.addEventListener("onEvalDataPath", new EventListener () {
				public boolean isAsap() {
					return false;
				}

				public void onEvent(Event event) {
					if (_component.getPage() != null)
						smartParse();
				}

			});
		}

		public void addSubscriber (XPathSubscriber subscriber)
		{
			_subscribers.add(subscriber);
		}
		
		public void removeSubscriber (XPathSubscriber subscriber)
		{
			_subscribers.remove(subscriber);
		}

		public String getDataPath ()
		{
			return _bind;
		}
		
		
		public void setParent (Component parent)
		{
			if (parent == null)
				unsubscribe();
			else
				doParse();
		}
		
		public void setPage (Page page)
		{
			if (page != null)
				doParse ();
			else
				unsubscribe();
		}
		
		public void setDataPath (String bind)
		{
			// Desuscribir
			if ( _bind != null && getDataSource() != null)
			{
				unsubscribe();
			}
			_bind = bind;
			doParse();
		}

		/*
		public void setDataPath (DataSource ds, Pointer pointer)
		{
			// Desuscribir
			if ( _bind != null && getDataSource() != null)
			{
				unsubscribe();
			}

			_ds = ds;
			_xPath = pointer.asPath();
			_bind = ds.toString()+":"+_xPath;
			parsePath ();
			subscribe();
		}

*/
		private void doParse() {
			if (_bind == null)
			{
				_xPath = null;
				_ds = null;
			}
			else if (_component.getPage() != null)
			{
				int i = _bind.indexOf(":");
				if ( i == -1)
				{
					String bind = _bind;
					if ( ! bind.startsWith("/") && ! bind.startsWith("["))
						bind = "/"+bind;
					parseRelativeBind (bind);
				}
				else
					parseAbsoluteBind (_bind.substring(0, i), _bind.substring(i+1));
			}
			
			else
			{
				_pendingParse = true;
				_xPath = null;
				_ds = null;
				Events.postEvent("onEvalDataPath", _component, null);
			}
		}

		/**
		 * 
		 */
		protected void unsubscribe() {
			String xPath = getXPathSubscription();
			if ( getDataSource() != null && xPath != null)
			{
				getDataSource().unsubscribeToExpression(xPath, this);
			}
		}
		
		private void parseAbsoluteBind(String ds, String path) {
		        IdSpace id = _component.getSpaceOwner();
		        String realDS = ds;
		        if (realDS.startsWith("/") && ! ds.startsWith("//"))
		            realDS = "//"+_component.getPage().getId()+realDS;
			try {
			    Component c = Path.getComponent(id, realDS);
	                        if (c instanceof DataSource)
	                        {
	                                _ds = (DataSource) c;
	                                _xPath = normalizePath(path);
	                                parsePath ();
	                                subscribe();
	                        }
	                        else
	                        {
	                            throw new UiException (realDS+" is not a DataSource");
	                        }
			} catch (ComponentNotFoundException e) {
			    throw new UiException ("Unable to parse "+ds+":"+path, e);
			}
		}

		/**
		 * 
		 */
		protected void subscribe() {
			String xPath = getXPathSubscription();
			if ( _ds != null && xPath != null)
			{
				_ds.subscribeToExpression(xPath, this);
			}
		}

		private void parseRelativeBind(String bind) {
			Component c = _component.getParent();
			while (c != null)
			{
				if ( c instanceof MasterRow && ((MasterRow) c).isMaster ||
						c instanceof MasterListItem)
				{
					_ds = null;
					_xPath = null;
					return;
				}
				else if ( c instanceof BindContext )
				{
					BindContext b = (BindContext) c;
					if (b.getDataSource() != null )
					{
						applyParentBind(bind, b);
						return;
					}
				}
				else
				{
					Object obj = c.getAttribute(BindContext.BINDCTX_ATTRIBUTE, Component.COMPONENT_SCOPE);
					if (obj != null && obj instanceof BindContext)
					{
						BindContext b = (BindContext) obj;
						if (b.getDataSource() != null)
						{
							applyParentBind(bind, b);
							return;
						}
						
					}
				}
				c = c.getParent();
			}

			
			throw new UiException ("wrong path "+bind);
		}

		/**
		 * @param bind
		 * @param b
		 */
		private void applyParentBind(String bind, BindContext b) {
			_ds = b.getDataSource();
			if (b.getXPath() == null)
				_xPath = null;
			else if ("/".equals(b.getXPath()))
				_xPath = normalizePath (bind);
			else
				_xPath = normalizePath (b.getXPath() + bind);
			parsePath();
			subscribe ();
		}

		private String normalizePath (String path)
		{
			if (path.endsWith("/."))
			{
				path = path.substring(0, path.length()-2);
			}
			if ( ! path.startsWith("/"))
				path = "/"+path;
			path = path.replaceAll("/\\./", "/");
			do {
				int first = path.indexOf("/../");
				if (first < 0)
					break;
				int toDelete = path.lastIndexOf('/', first - 1);
				if (toDelete < 0)
					break;
				path = path.substring(0, toDelete) + path.substring(first+3);
			}
			while (true); 
			return path;
		}

		/**
		 * 
		 */
		protected abstract void parsePath() ;
		
		public DataSource getDataSource ()
		{
			smartParse();
			return _ds;
		}

		public void onUpdate (XPathEvent event) {
			if ( event instanceof XPathRerunEvent && ! _processingXPathRerunEvent)
			{
				unsubscribe();
				_pendingParse = true;
				_processingXPathRerunEvent = true;
				try {
					smartParse();
				} finally {
					_processingXPathRerunEvent = false;
				}
			}
			XPathSubscriber subscribers [] = (XPathSubscriber[]) _subscribers.toArray(new XPathSubscriber[0]);
			for (int i = 0; i < subscribers.length; i ++)
			{
				subscribers[i].onUpdate(event);
			}

			if ( event instanceof XPathRerunEvent)
				Events.postEvent(new Event ("onChangeXPath", _component, event.getXPath()));
		}

		public String getXPath ()
		{
			return _xPath;
		}
		
		protected String getXPathSubscription ()
		{
			return _xPath;
		}

		/**
		 * @return Returns the _component.
		 */
		public Component getComponent() {
			return _component;
		}
		
		public abstract boolean isValid ();
		
		public boolean isVoid ()
		{
			return _bind == null; 
		}

}

