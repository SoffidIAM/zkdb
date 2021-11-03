package es.caib.zkib.binder.list;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.ListitemComparator;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;

import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.component.DataListbox;
import es.caib.zkib.component.DataListcell;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.Pointer;

public class PartialModelProxy implements XPathSubscriber, ModelProxy, ListModelExt, Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Vector v = null;
	transient Vector listeners;
	CollectionBinder binder;
	DataModelCollection model;
	
	public PartialModelProxy(CollectionBinder binder) {
		this.binder = binder;
		
		listeners = new Vector ();
		v = new Vector ();
		model = binder.getUpdateableListModel();
		
		populateData();
		
		binder.addSubscriber(this);
		
	}

	/**
	 * @param model
	 */
	private void populateData() {
		v.clear();
		List pointers = binder.getPointers();
		if (pointers != null)
		{
			for (int i = 0; i < pointers.size(); i++)
			{
				Pointer p = (Pointer) pointers.get(i);
				DataModelNode data = (DataModelNode) p.getValue();
				int position = binder.getUpdateableListModel().indexOf(data);
				if (position >= 0)
					v.add( new Integer (position));
			}
		}
	}
	

	public Object getElementAt(int index) {
		if (index < 0 || index >= v.size())
			return null;
		Integer i = (Integer) v.get(index);
		if ( i == null)
			return null;
		else
			return binder.getUpdateableListModel().getDataModel(i.intValue());
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
		
		if (event.getType() == XPathCollectionEvent.ADDED)
		{
			// Por defecto no se añade
		}
		if (event.getType() == XPathCollectionEvent.DELETED)
		{
			int i = v.indexOf(new Integer (event.getIndex()));
			if ( i >= 0)
			{
				v.remove(i);
				sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, i, i));
			}
		}
		if (event.getType() == XPathCollectionEvent.RECREATED)
		{
			int size = v.size() ;
			v.clear();
			
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size-1));
			
			populateData();

			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, v.size()-1));
		}
        if (event.getType() == XPathCollectionEvent.UPDATED) {
			int i = v.indexOf(new Integer (event.getIndex()));
			if ( i >= 0)
			{
				sendEvent ( new ListDataEvent(this, SoffidListDataEvent.INTERVAL_UPDATED, i, i));
			}
        }
	}

	public void onUpdate (XPathEvent event)
	{
		if (event instanceof XPathCollectionEvent)
			onListChange ( (XPathCollectionEvent) event);
		else if (event instanceof XPathRerunEvent)
		{
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0,v.size()-1));
			model = binder.getUpdateableListModel();
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
		
		Integer i = (Integer) v.get(index);
		if ( i == null)
			return null;
		else
			return "["+(i.intValue()+1)+"]";
	}

	public int newInstance() throws Exception {
		DataModelNode dm = model.newInstance();
		int i = model.indexOf(dm);
		v.add(new Integer(i));
		sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, v.size()-1,v.size()-1));
		return v.size()-1;
	}

	public int getPosition(int xpathIndex) {
		return -1;
	}

	public void unsubscribe() {
		binder.removeSubscriber(this);
	}
	
	public void sort(Comparator cmpr, boolean ascending) {
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
			Collections.sort(v, new DataNodeComparator(binder.getUpdateableListModel(), bind, ascending));
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
