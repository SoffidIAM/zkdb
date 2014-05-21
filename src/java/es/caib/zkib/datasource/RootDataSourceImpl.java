package es.caib.zkib.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Textbox;

import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathException;

public class RootDataSourceImpl {
	Object data;
	JXPathContext context;
	private HashMap registry = new HashMap();
	DataSource ds;
	Log log = Log.lookup(RootDataSourceImpl.class);
	
	public RootDataSourceImpl(DataSource ds, Object data) {
		this.ds = ds;
		this.data = data;
	}
	
	public void setData (Object data)
	{
		this.data = data;
		context = null;
		sendEvent(new XPathRerunEvent(ds, "/"));
	}

	public JXPathContext getJXPathContext ()
	{
		if (context == null)
		{
			context = new JXPContext (ds, data);
			context.setIgnoreExceptions(false);
		}
		return context;
	}

	private String getParentPath (String path)
	{
        if ("/".equals(path))
            return null;
		int i = path.lastIndexOf('[');
		int j = path.lastIndexOf('/');
		if ( i > 0 && i >= j)	// Nota: Estrictamente mayor que cero, para no generar cadena vacía
			return path.substring(0, i);
		if ( j > 0 && j >= i)
			return path.substring(0, j);
		return "/";
	}
	
	/**
	 * @param path
	 * @return
	 */
	private SubscriberInfo getSubscriberInfo(String path, boolean create) {
		SubscriberInfo info = null;
		if (registry.containsKey(path)) {
			info = (SubscriberInfo) registry.get(path);
		} else if (create){
			info = new SubscriberInfo ();
			registry.put (path, info);
		}
		return info;
	}

	private void addParentSubscription ( String parent, String child)
	{
		SubscriberInfo info = getSubscriberInfo(parent, true);
		if ( !info.childSubscriptions.contains(child))
		{
			info.childSubscriptions.add(child);
			String grandParent = getParentPath (parent);
			if ( grandParent != null)
				addParentSubscription(grandParent, parent);
		}
	}

	private void removeParentSubscription ( String parent, String child)
	{
		SubscriberInfo info = getSubscriberInfo(parent, false);
		if (info != null)
		{
			info = (SubscriberInfo) registry.get(parent);
			info.childSubscriptions.remove(child);
			if (info.childSubscriptions.isEmpty() && info.subscribers.isEmpty())
			{
				String grandParent = getParentPath (parent);
				if ( grandParent != null)
					removeParentSubscription(grandParent, parent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#subscribeToExpression(es.caib.zkib.jxpath.Pointer, org.zkoss.zk.ui.Component)
	 */
	public void subscribeToExpression(String path, XPathSubscriber subscriber) {
		log.debug("SUBSCRIBE: "+toString()+" on "+path+" Component="+subscriber.toString());

		SubscriberInfo info = getSubscriberInfo(path, true);
		info.subscribers.add(subscriber);
		String parent = getParentPath(path);
		if (parent != null)
			addParentSubscription(parent, path);
	}
	
	public void unsubscribeToExpression(String xpath, XPathSubscriber subscriber) {
		log.debug ("UNSUBSCRIBE: "+toString()+" on "+xpath+" Component="+subscriber.toString());

		SubscriberInfo info = getSubscriberInfo(xpath, false);
		if ( info != null)
		{
			info.subscribers.remove(subscriber);
			if (info.subscribers.isEmpty() && info.childSubscriptions.isEmpty())
			{
				String parent = getParentPath(xpath);
				if (parent != null)
					removeParentSubscription(parent, xpath);
			}
		}
	}
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#updateDisplays(es.caib.zkib.jxpath.Pointer)
	 */
	public void sendEvent (XPathEvent event) {
		// Evento a notificar a todos los hijos
		log.debug ("SENDEVENT: "+event);
		SubscriberInfo info = getSubscriberInfo(event.getXPath(), false);
		if (info != null && ! info.subscribers.isEmpty()) {
			XPathSubscriber subscribers[] = (XPathSubscriber[]) info.subscribers.toArray(new XPathSubscriber[0]);
			for ( int j = 0; j < subscribers.length; j++)
			{
				log.debug ("SENDEVENT "+event.toString()+" to "+subscribers[j]);
				subscribers[j].onUpdate(event);
			}
		}
		// Ahora enviar el evento XPathRerunEvent a los hijos
		if ( (event instanceof XPathRerunEvent || event instanceof XPathValueEvent) &&
			 info != null && ! info.childSubscriptions.isEmpty())
		{
			String keys[] = (String[]) info.childSubscriptions.toArray(new String[0]);
			for (int i = 0; i < keys.length; i++)
			{
				XPathRerunEvent newEvent = new XPathRerunEvent (event.getDataSource(), keys[i]);
				sendEvent(newEvent);
			}
 		}
		// Marcar el padre como modificado
		if ( event instanceof XPathValueEvent)
		{
			JXPathContext ctx = getJXPathContext();
			String path = event.getXPath();
			Object value;
			try {
				do
				{
					path = getParentPath (path);
					if (path == null)
						return;
					value = ctx.getValue(path);
				} while (value != null &&  ! ( value instanceof DataModelNode) );
				if (value != null)
					( (DataModelNode) value ).update();
			} catch (JXPathException e) {
				
			}
		}
	}

	class SubscriberInfo {
		Set childSubscriptions = new HashSet();
		Set subscribers = new HashSet ();
	}
	
	public String toString ()
	{
		return ds.toString();
	}

	public void focus(String xpath) {
		SubscriberInfo info = (SubscriberInfo) registry.get(xpath);
		if (info != null && ! info.subscribers.isEmpty()) {
			XPathSubscriber subscribers[] = (XPathSubscriber[]) info.subscribers.toArray(new XPathSubscriber[0]);
			for ( int j = 0; j < subscribers.length; j++)
			{
				if (subscribers[j] instanceof Textbox)
				{
					((Textbox) subscribers[j]).focus();
				}
			}
		}
	}
}

