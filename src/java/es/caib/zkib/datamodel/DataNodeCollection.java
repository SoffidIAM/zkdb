package es.caib.zkib.datamodel;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.beanutils.ConstructorUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;

import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;


public class DataNodeCollection implements List, DataModelCollection, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Class clazz;
	Vector elements = new Vector ();
	DataContext ctx;
	Future<?> delayedList = null;
	int lastDelayedListMember;

	private boolean _dirty = true;
	private boolean _firstRefresh = true;
	Finder finder;
	int nextKey = 0;
	
	
	public boolean isDirty () {
		return _dirty;
	}
	
	protected DataNodeCollection(DataContext ctx, Class clazz, Finder finder) {
		this.clazz = clazz;
		this.finder = finder;
		this.ctx = ctx;
	}


	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#getElementAt(int)
	 */
	public DataModelNode getDataModel(int index) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		DataModelNode model = (DataModelNode) elements.get(index);
		if (model == null || model.isDeleted())
			return null;
		else
			return model;
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#getSize()
	 */
	public int getSize() {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.size();
	}

	private synchronized void smartRefresh() throws Exception {
		if (_dirty)
		{
			cancel();
			// Purgar las tablas
			elements.removeAllElements();
			
			// Repopular las tablas
			Collection coll = null;
			try {
				if ( !ctx.getParent().isNew() || !_firstRefresh)
					coll = finder.find();
				else if (finder instanceof ExtendedFinder && ((ExtendedFinder) finder).findOnNewObjects())
					coll = finder.find();
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			
			if (coll != null)
			{
				delayedList = coll instanceof Future ? (Future) coll: null;
				lastDelayedListMember = 0;
				Iterator it = coll.iterator();
				while (it.hasNext())
				{
				    Object obj = it.next();
				    populate (obj);
					lastDelayedListMember++;
				}
			}
			
			_dirty = false;
			_firstRefresh = false;
			int newsize = elements.size();
			// Enviar evento
//			getDataSource().sendEvent(new XPathCollectionEvent (getDataSource(), getXPath(), XPathCollectionEvent.RECREATED, newsize));
			getDataSource().sendEvent(new XPathRerunEvent(getDataSource(), getXPath()));
		}
	}
	
	private String getXPath() {
		return ctx.getParent().getXPath() + "/" + ctx.getXPath();
	}


	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#commit()
	 */
	public void prepareCommit() throws CommitException {
		// Primero se borra lo borrable
		Iterator it = elements.iterator();
		while (it.hasNext())
		{
			DataModelNode model = (DataModelNode) it.next ();
			if (model != null && model.isDeleted())
			{
				try {
					model.prepareCommit();
				} catch (CommitException e) {
					model.undelete();
					throw e;
				}
			}
		}
		
		// Después se actualiza e inserta
		it = elements.iterator();
		while (it.hasNext())
		{
			DataModelNode model = (DataModelNode) it.next ();
			if (model != null && ! model.isDeleted())
			{
				try {
					model.prepareCommit();
				} catch (CommitException e) {
					model.undelete();
					throw e;
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#commit()
	 */
	public void commit() {
		if (finder instanceof ExtendedFinder && ((ExtendedFinder) finder).refreshAfterCommit())
		{
			try {
				refresh();
			} catch (Exception e) {
				setDirty();
			}
		} else {
			// Se eliminan de la lista de elmentos
			for ( int i = 0; i < elements.size(); i++)
			{
				DataModelNode model = (DataModelNode) elements.get(i);
				if (model != null && model.isDeleted())
				{
					elements.set(i, null);
				}
				if (model != null)
					model.commit();
			}
		}
		
	}

	private void populate (Object data)
	{
		DataNode dataModel = encapsulateData(data);
		internalAdd(dataModel);
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#indexOf(java.lang.Object)
	 */
	public int indexOf(DataModelNode model) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.indexOf(model);
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#refresh()
	 */
	public void refresh () throws Exception
	{
		setDirty();
		_firstRefresh = false;
		smartRefresh();
	}
	
	public void setDirty() {
		_dirty = true;
	}

	public void onDelete (DataModelNode model) {
		if ( !model.isDeleted())
			model.delete();
		else
		{
			int i = indexOf (model);
			if (i >= 0 && model.isDeleted())
			{
				getDataSource().sendEvent ( new XPathCollectionEvent (getDataSource(), getXPath(), XPathCollectionEvent.DELETED,i ));
			}
		}
	}

	public void onUndelete (DataModelNode model) {
		if ( model.isDeleted())
			model.undelete();
		else
		{
			int i = indexOf (model);
			if (i >= 0 && ! model.isDeleted())
			{
				getDataSource().sendEvent (new XPathCollectionEvent (getDataSource(), getXPath(), XPathCollectionEvent.ADDED,i ));
			}
		}
	}

	protected void onInsert(DataModelNode model) {
		int i = elements.indexOf(model);
		getDataSource().sendEvent ( new XPathCollectionEvent (getDataSource(), getXPath(), XPathCollectionEvent.ADDED,i ));
	}

	public void onUpdate(DataModelNode model) {
		// Nothing to do
	}
	
	public void setActiveNode (DataModelNode model) {
		int i = indexOf(model);
		getDataSource().sendEvent(new XPathCollectionEvent (getDataSource(), getXPath(), XPathCollectionEvent.FOCUSNODE, i ));
	}

	/**
	 * Implements java.util.Collection.add
	 */
	public boolean add (Object data)
	{
		try {
			smartRefresh();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		DataModelNode node = insert (data);
		Events.postEvent("onChange", (Component) getDataSource(), null);
		return true;
	}
	
	protected boolean internalAdd (Object data)
	{
		DataNode node = (DataNode) data;
		if (elements.add(node))
		{
			return true;
		}
		else
			return false;
	}

	protected DataModelNode insert (Object data)
	{
		DataNode dataModel = encapsulateData(data);
		dataModel.setNew();
		internalAdd (dataModel);
		onInsert(dataModel);
		dataModel.update();
		return dataModel;
	}


	/**
	 * @param data
	 * @return
	 */
	private DataNode encapsulateData(Object data) {
		if (data == null)
		{
			throw new NullPointerException ("Cannot add null to "+getXPath());
		}
		DataNode dataModel;
		try {
			DataContext newCtx = new DataContext();
			newCtx.setParent(ctx.getParent());
			newCtx.setListModel(this);
			newCtx.setDataSource(ctx.getDataSource());
			newCtx.setXPath(ctx.getXPath()+"["+(elements.size()+1)+"]");
			newCtx.setData(data);
			newCtx.setCustomData(ctx.getCustomData());
			Object args [] = { newCtx };
			dataModel = (DataNode) ConstructorUtils.invokeConstructor(clazz, args);
			newCtx.setCurrent(dataModel);
		} catch (InstantiationException e) {
			throw new RuntimeException (e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException (e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e);
		}
		return dataModel;
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableListModel#newInstance()
	 */
	public DataModelNode newInstance () throws Exception
	{
		smartRefresh();
		Object data = finder.newInstance();
		DataModelNode node = insert (data);
		Events.postEvent("onChange", (Component) getDataSource(), null);
		return node;
	}
	
	public DataModelNode getParentDataNode() {
		return ctx.getParent();
	}
/*
	public UpdateableDataModel getRootDataNode() {
		return rootDataNode;
	}
*/	
	/** Implemetnación de la Collection */

	public int size() {
		try {
			smartRefresh ();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		return elements.size ();
	}

	public void clear() {
		setDirty();
	}

	public boolean isEmpty() {
		try {
			smartRefresh ();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		return elements.isEmpty();
	}

	public Object[] toArray() {
		try {
			smartRefresh ();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		return elements.toArray();
	}

	public boolean contains(Object o) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		return elements.contains(o);
	}

	public boolean remove(Object o) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		((DataModelNode)o).delete();
		return true;
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException ("Method not supported");
	}

	public boolean containsAll(Collection c) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.containsAll(c);
	}
	
	public boolean removeAll(Collection c) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		Iterator it = c.iterator();
		while (it.hasNext() )
		{
			remove (it.next());
		}
		return true;
	}

	public boolean retainAll(Collection c) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		for (int i = 0; i < elements.size(); i++)
		{
			DataModelNode data = getDataModel(i);
			if (! c.contains(data))
				data.delete ();
		}
		return true;
	}

	public Iterator iterator() {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.iterator();
	}

	public Object[] toArray(Object[] a) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.toArray(a);
	}

	public Object get(int index) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.get(index);
	}

	public Object remove(int index) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		Object o = get(index);
		if ( o != null )
			remove (o);
		return o;
	}

	public void add(int index, Object element) {
		throw new UnsupportedOperationException ("Unimplemented method");
	}

	public int indexOf(Object o) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.lastIndexOf(o);
	}

	public boolean addAll(int index, Collection c) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		throw new UnsupportedOperationException ("Unimplemented method");
	}

	public List subList(int fromIndex, int toIndex) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.subList( fromIndex, toIndex );
	}

	public ListIterator listIterator() {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.listIterator();
	}

	public ListIterator listIterator(int index) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		return elements.listIterator(index);
	}

	public Object set(int index, Object element) {
		try {
			smartRefresh ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		DataNode dn = (DataNode) elements.get(index);
		dn.getDataContext().setData(element);
		dn.update();
		throw new UnsupportedOperationException ("Unimplemented method");
	}


	/**
	 * @return Returns the datasource.
	 */
	public DataSource getDataSource() {
		return ctx.getDataSource();
	}
	


	public void refreshAfterCommit()  {
		if (finder instanceof ExtendedFinder && ((ExtendedFinder)finder).refreshAfterCommit()) 
			try {
				if (! _firstRefresh)
					refresh();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}


	public boolean isInProgress() {
		if ( delayedList == null ) 
			return false;
		if (delayedList.isCancelled())
			return false;
		if (delayedList.isDone() && lastDelayedListMember >= ((Collection)delayedList).size())
			return false;
		return true;
	}

	public void updateProgressStatus() {
		if (delayedList != null)
		{
			if (delayedList.isCancelled())
			{
				try {
					delayedList.get();
				} catch (ExecutionException e) {
					throw new UiException(e.getCause());
				} catch (InterruptedException e) {
					// Ignore
				}
			}
			else
			{
				Iterator it = ((Collection) delayedList).iterator();
				int i = 0;
				while (it.hasNext())
				{
				    Object obj = it.next();
					if ( i >= lastDelayedListMember)
					{
					    populate (obj);
						lastDelayedListMember++;
						getDataSource().sendEvent(new XPathCollectionEvent(getDataSource(), getXPath(), XPathCollectionEvent.ADDED, elements.size()-1));
					}
					i++;
				}
			}
		}
	}


	public void cancel() {
		if (delayedList != null)
			delayedList.cancel(false);
	}

	public Finder getFinder() {
		return finder;
	}

	public boolean updateBeforeParent() {
		if (finder instanceof ExtendedFinder)
			return ((ExtendedFinder) finder).updateBeforeParents();
		else
			return false;
	}

}

