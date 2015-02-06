package es.caib.zkib.binder.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModelExt;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.ListitemComparator;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;

import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.component.DataDatebox;
import es.caib.zkib.component.DataIntbox;
import es.caib.zkib.component.DataLabel;
import es.caib.zkib.component.DataListbox;
import es.caib.zkib.component.DataListcell;
import es.caib.zkib.component.DataTextbox;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;

public class FullModelProxy implements ModelProxy, XPathSubscriber, ListModelExt {
	DataModelCollection model;
	/**
	 * Vector de Pointers.
	 */
	Vector v = null;
	Vector listeners;
	CollectionBinder binder;
	
	public FullModelProxy(CollectionBinder binder) {
		this.binder = binder;
		this.model = binder.getUpdateableListModel();

		listeners = new Vector ();
		v = new Vector ();

		populateData(model);
		
		binder.addSubscriber(this);
		// No se ha de registrar, porque ya se encarga el binder
		// binder.getDataSource().subscribeToExpression(binder.getXPath(), this);
		
	}

	/**
	 * @param model
	 */
	private void populateData(DataModelCollection model) {
		v.clear();
		for (int i = 0; model != null && i < model.getSize(); i++)
		{
			DataModelNode data = model.getDataModel(i);
			if (data != null)
				v.add( new Integer (i));
		}
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.ModelProxy#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		Integer i = (Integer) v.get(index);
		if ( i == null)
			return null;
		else
			return model.getDataModel(i.intValue());
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

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.ModelProxy#getSize()
	 */
	public int getSize() {
		return v.size();
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.ModelProxy#addListDataListener(org.zkoss.zul.event.ListDataListener)
	 */
	public void addListDataListener(ListDataListener l) {
		listeners.add( l );

	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.ModelProxy#removeListDataListener(org.zkoss.zul.event.ListDataListener)
	 */
	public void removeListDataListener(ListDataListener l) {
		listeners.remove( l );
	}

	public void onListChange(XPathCollectionEvent event) {
		if (event.getType() == XPathCollectionEvent.ADDED)
		{
			v.add(new Integer (event.getIndex()));
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, v.size()-1,v.size()-1));
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
			int i = v.size ();
			v.clear();
			
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, i-1));
			
			populateData(model);

			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, v.size()-1));
		}
	}

	public void onUpdate (XPathEvent event)
	{
		if (event instanceof XPathCollectionEvent)
			onListChange ( (XPathCollectionEvent) event);
		else if (event instanceof XPathRerunEvent)
		{
			int i = v.size ();
			v.clear();

			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0,i-1));
			model = binder.getUpdateableListModel();
			populateData(model);
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

	public int newInstance() throws Exception {
	    DataModelNode obj = model.newInstance();
	    //TODO: pareix un BUG
	    //v.add (new Integer (model.indexOf(obj)));
	    return v.size()-1;
	}

	public int getPosition(int xpathIndex) {
		return v.indexOf( new Integer (xpathIndex));
	}

	public void unsubscribe() {
		binder.removeSubscriber(this);
	}

	private String searchBindOnChildren (Component parent)
	{
		for (Component child: (List<Component>)parent.getChildren())
		{
			if (child instanceof DataLabel && ((DataLabel) child).getBind() != null)
				return ((DataLabel) child).getBind();
			if (child instanceof DataTextbox && ((DataTextbox) child).getBind() != null)
				return ((DataTextbox) child).getBind();
			if (child instanceof DataDatebox && ((DataDatebox) child).getBind() != null)
				return ((DataDatebox) child).getBind();
			if (child instanceof DataIntbox && ((DataIntbox) child).getBind() != null)
				return ((DataIntbox) child).getBind();
			String b = searchBindOnChildren(child);
			if (b != null)
				return b;
		}
		return null;
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
			if (bind == null)
				bind = searchBindOnChildren (cell);
			
			if (bind == null)
				return;
			
			Vector newVector = v;
			Collections.sort(newVector, new DataNodeComparator(binder.getUpdateableListModel(), bind, ascending));
			
			v = new Vector ();
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0,newVector.size()-1));
			v = newVector;
			sendEvent ( new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0,newVector.size()-1));
			StringBuffer order = new StringBuffer("Order =");
			for (int j = 0; j < v.size(); j++)
			{
				order.append(v.get(j)+" ");
			}
		}
	}

	
}
