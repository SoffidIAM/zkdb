package es.caib.zkib.binder.list;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemComparator;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;

import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.component.DataListbox;
import es.caib.zkib.component.DataListcell;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.Pointer;

public class ListModelProxy implements XPathSubscriber, ModelProxy, ListModelExt, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DataModelCollection model;
	/**
	 * Vector de punteros (Pointers)
	 * Cada entrada contiene el pointer a la fila correspondiente
	 */
	List v = null;
	transient Vector listeners;
	CollectionBinder binder;
	
	
	public ListModelProxy(CollectionBinder binder) {
		this.binder = binder;
		
		listeners = new Vector ();
		v = new Vector ();

		populateData();
		
		binder.addSubscriber(this);
		
	}

	/**
	 * @param model
	 */
	private void populateData() {
		Collection<Pointer> pointers = binder.getPointers();
		v = new Vector();
		if (pointers != null)
			for (Pointer pointer: pointers)
			{
				v.add (pointer.asPath());
			}
	}
	

	public Object getElementAt(int index)
	{
		if (index < 0 || index >= v.size())
			return null;
		String p = (String) v.get(index);
		if (p == null)
			return null;
		
		else
		{
			if (binder.getJXPathContext() != null)
			{
				return binder.getJXPathContext().getValue(p);
			}
			
			else
			{
				return null;
			}
		}
	}

	public int getSize() {
		return v.size();
	}

	public void addListDataListener(ListDataListener l) {
		listeners.add( l );
	}

	public void removeListDataListener(ListDataListener l) {
		listeners.remove( l );
	}

	public void onListChange(XPathCollectionEvent event) {
		String path = event.getXPath()+"["+(event.getIndex()+1)+"]";
		String prefix = binder.getDataSource().getRootPath();
		if (path.startsWith(prefix))
		{
			path = path.substring(prefix.length());
			binder.getDataSource().sendEvent(new XPathValueEvent(binder.getDataSource(), binder.getXPath()));
		}
		if (event.getType() == XPathCollectionEvent.ADDED)
		{
			v.add(path);
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, v.size()-1,v.size()-1));
		}
		if (event.getType() == XPathCollectionEvent.DELETED)
		{
			int i = v.indexOf( path );
			if ( i >= 0)
			{
				v.remove(i);
				sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, i, i));
			}
		}
		if (event.getType() == XPathCollectionEvent.RECREATED)
		{
			int size = v.size ();
			
			v.clear();
			
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size-1));
			
			binder.setDataPath(binder.getDataPath());
			populateData();

			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, v.size()-1));
		}
	}

	public void onUpdate (XPathEvent event)
	{
		if (event instanceof XPathCollectionEvent)
			if(binder.getDataSource() != null)	//Ignorem els events si l'element s'ha eliminat
				onListChange ( (XPathCollectionEvent) event);
		else if (event instanceof XPathRerunEvent )
		{
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0,v.size()-1));
			populateData();
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0,v.size()-1));
		}
	}

	private void sendEvent(ListDataEvent event) {
		Iterator it = listeners.iterator();
		while (it.hasNext())
		{
			ListDataListener listener = (ListDataListener) it.next ();
			listener.onChange(event);
		}
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.ModelProxy#getBind(int)
	 */
	public String getBind (int index)
	{
		if ( index < 0 || index >= v.size())
			return null;
		
		String path = (String) v.get(index);
		if ( path == null)
			return null;
		else
		{
		    Component component = (Component) binder.getDataSource();
		    String ds = "//"+component.getPage().getId()+Path.getPath(component);
		    return ds+":"+path;
		}
	}

	public int newInstance() throws Exception {
		throw new UnsupportedOperationException ("cannot add new records");
	}

	public int getPosition(int xpathIndex) {
		String xpath = binder.getDataPath()+"["+xpathIndex+"]";
		return v.indexOf( xpath );
	}

	public void unsubscribe() {
		binder.removeSubscriber(this);
	}

	public void sort(Comparator cmpr, boolean ascending)
	{
		if (cmpr instanceof ListitemComparator)
		{
			ListitemComparator lic = (ListitemComparator) cmpr;
			org.zkoss.zul.Listheader header = lic.getListheader();
			DataListbox box = (DataListbox) header.getListbox();
			Listhead head = box.getListhead();
			int i = head.getChildren().indexOf(header);
			
			DataListcell cell = (DataListcell) box.getMasterListItem().getChildren().get(i);
			
			String bind = cell.getBind();
			
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0,v.size()-1));
			Collections.sort(v, new XPathComparator(binder.getDataSource().getJXPathContext(), bind, ascending));
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0,v.size()-1));
		}
	}

	//-- Serializable --//
	private synchronized void readObject(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		listeners = new Vector();
	}
}
