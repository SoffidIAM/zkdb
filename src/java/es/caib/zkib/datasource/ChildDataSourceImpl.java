package es.caib.zkib.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Component;

import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathException;
import es.caib.zkib.jxpath.Pointer;
import es.caib.zkib.jxpath.Variables;
import es.caib.zkib.jxpath.ri.model.beans.NullPointer;

public class ChildDataSourceImpl implements DataSource {
	DataSource parent;
	String xpathBase;
	String currentPath;
	Pointer pointer;
	JXPathContext context;
	private HashMap registry = new HashMap();
	Log log = Log.lookup(ChildDataSourceImpl.class);
	
	public ChildDataSourceImpl() {
	}
	
	public void setDataSource(DataSource parent) {
		if (this.parent != null && parent != this.parent)
			throw new RuntimeException ("Cannot change parent data source");
		this.parent = parent;
	}
	
	public void setRootXPath (String xPath) {
		if (xPath != null && xPath.endsWith("/"))
			xpathBase = xPath.substring(0, xPath.length()-1);
		else
			this.xpathBase = xPath;
		
		setXPath (null);
	}
	
	public synchronized void setXPath (String newPath)
	{
		String fullPath = parsePath(newPath);
		if (!fullPath.equals(currentPath))
		{
			String oldPath = currentPath;
			context = null;
			currentPath = fullPath;
			try {
				pointer = parent.getJXPathContext().getPointer(currentPath);
			} catch (JXPathException e) {
				pointer = new NullPointer (null, parent.getJXPathContext().getLocale());
			}

			// Reregistrar todos los hijos
			String keys[] = (String []) registry.keySet().toArray(new String[0]);
			XPathRerunEvent event = new XPathRerunEvent(this, "/");
			for (int i = 0; i < keys.length; i++)
			{
				String path = keys[i];
				Set subscriberSet = (Set) registry.get(path);
				if ( subscriberSet != null)
				{
					XPathSubscriber subscribers[] = (XPathSubscriber[]) subscriberSet.toArray(new XPathSubscriber[0]);
					for ( int j = 0; j < subscribers.length; j++)
					{
						// Se le desconecta del padre, dado que al haber actualizado
						// currentPath, el subscriptor no sabrá desconectarse
						if ( oldPath != null)
							parent.unsubscribeToExpression(oldPath + path, subscribers[j]);
						log.debug("SENDMESSAGE "+event+" to "+subscribers[j] );
						subscribers[j].onUpdate(event);
					}
				}
			}
//			parent.sendEvent( new XPathRerunEvent (parent, oldPath));
		}
	}

	/**
	 * @param newPath
	 * @return
	 */
	private String parsePath(String newPath) {
		String fullPath;
		if (newPath == null)
			fullPath = "/void";
		else
		{
			int i = newPath.indexOf(":/");
			if ( i == -1)
			{
				fullPath = xpathBase + newPath;
			}
			else
			{
				String dsName = newPath.substring(0, i);
				Component c = org.zkoss.zk.ui.Path.getComponent(dsName);
				if (c != parent)
				{
					throw new RuntimeException ("Cannot change data source to "+dsName);
				}
				fullPath = newPath.substring(i+1); 
			}
		}
		return fullPath;
	}

	public JXPathContext getJXPathContext ()
	{
		if (context == null && pointer != null)
		{
			JXPathContext ctx = parent.getJXPathContext();
			context = ctx.getRelativeContext(pointer);
		}
		return context;
	}

	private String getParentPointer (String path)
	{
		if ("/".equals(path) || ".".equals(path))
			return currentPath;
		else
			return currentPath + path;
	}
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#subscribeToExpression(es.caib.zkib.jxpath.Pointer, org.zkoss.zk.ui.Component)
	 */
	public synchronized void subscribeToExpression(String path, XPathSubscriber subscriber) {
		log.debug("SUBSCRIBE: "+toString()+" on "+path+" Component="+subscriber.toString());
		parent.subscribeToExpression(getParentPointer(path), subscriber);
		if (registry.containsKey(path)) {
			Set subscriberSet = (Set) registry.get(path);
			subscriberSet.add(subscriber);
		} else {
			Set subscriberSet = new HashSet();
			subscriberSet.add(subscriber);
			registry.put(path,subscriberSet);
		}
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#updateDisplays(es.caib.zkib.jxpath.Pointer)
	 */
	public void sendEvent(XPathEvent event) {
		String newPath = getParentPointer(event.getXPath ());
		if (event instanceof XPathRerunEvent)
		{
			parent.sendEvent(new XPathRerunEvent (parent, newPath));
		}
		else if (event instanceof XPathValueEvent) 
		{
			parent.sendEvent(new XPathValueEvent (parent, newPath));
		}
		else if (event instanceof XPathCollectionEvent)
		{
			XPathCollectionEvent event2 = (XPathCollectionEvent) event;
			parent.sendEvent (new XPathCollectionEvent(parent, newPath, event2.getType(), event2.getIndex()));
		}
	}

	public synchronized void unsubscribeToExpression(String xpath, XPathSubscriber subscriber) {
		log.debug("UNSUBSCRIBE: "+toString()+" on "+xpath+" Component="+subscriber.toString());
		Set subscriberSet = (Set) registry.get(xpath);
		if (subscriberSet != null)
			subscriberSet.remove(subscriber);
		parent.unsubscribeToExpression(xpath, subscriber);
	}

	public void commit() throws CommitException {
		parent.commit() ;
		
	}
	
	public boolean isCommitPending ()
	{
		return parent.isCommitPending();
	}

	public Variables getVariables() {
		return parent.getVariables();
	}
	
	public String toString ()
	{
		return parent.toString()+":"+xpathBase;
	}

	public String getRootPath() {
		String root = parent.getRootPath();
		String suffix = getParentPointer("/");
		if (root.endsWith("/") && suffix.startsWith("/"))
		{
			return root+suffix.substring(1);
		}
		else if ( ! root.endsWith("/") && ! suffix.startsWith("/"))
		{
			return root + "/" + suffix;
		}
		else
			return root + suffix;
	}
}
