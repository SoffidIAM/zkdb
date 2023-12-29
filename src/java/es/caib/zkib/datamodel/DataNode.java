package es.caib.zkib.datamodel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaMap;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.WrapDynaClass;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;

import es.caib.zkib.binder.SmartEvents;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;


public abstract class DataNode implements DataModelNode, DynaBean, Map, Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	HashMap<String,DataNodeCollection> children;
	protected List dynaProperties;
	protected HashMap dynaPropertiesMap;
	private boolean _new = false;
	private boolean _deleted = false;
	private boolean _updated = false;
	private boolean _childrenUpdated = false;
	private DataNodeDynaClass dynaClass;
	DataContext ctx;
	private long _lastUpdated = 0;
	protected LazyDynaMap lazyMap = null;
	boolean _transient = false;
	
	
	public DataNode (DataContext ctx)
	{		
		this.ctx = ctx;
		if (ctx.getData() != null && ctx.getData() instanceof Map)
			lazyMap = new LazyDynaMap((Map)ctx.getData());
		init ();
	}
	
	public boolean isTransient() {
		return _transient;
	}

	public void setTransient(boolean _transient) {
		this._transient = _transient;
	}

	private void init ()
	{
		children = new HashMap();
		dynaProperties = new Vector();
		dynaPropertiesMap = new HashMap ();
		DynaProperty dp = new DynaProperty ("instance", Object.class);
		dynaProperties.add(dp);
		dynaPropertiesMap.put("instance", dp);
	}

	protected void addFinder (String name, Finder finder, Class clazz)
	{
		addFinder (name, finder, clazz, null);
	}
	
	protected void addFinder (String name, Finder finder, Class clazz, Object customData)
	{
		DataContext ctx2 = new DataContext ();
		ctx2.setParent(this);
		ctx2.setDataSource(ctx.getDataSource());
		ctx2.setXPath(name);
		ctx2.setCustomData(customData);

		DataNodeCollection listModel = new DataNodeCollection(ctx2, clazz, finder);
		if ( ! _new)
			listModel.setDirty();
		
		children.put(name, listModel);
		DynaProperty dp = new DynaProperty (name, DataNodeCollection.class);
		dynaProperties.add(dp);
		dynaPropertiesMap.put(name, dp);
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#getListModel(java.lang.String)
	 */
	public DataModelCollection getListModel (String name)
	{
		return (DataModelCollection) children.get(name);
	}

	public void prepareCommit () throws CommitException
	{
		if (_transient)
			return;
		
		long l = _lastUpdated;
		try {
			if (_deleted && ! _new)
			{
				updateChildCollections(false);
				doDelete ();
				l = _lastUpdated;
			}
			else if (_new && ! _deleted)
			{
				doInsert ();
				l = _lastUpdated;
				updateChildCollections(false);
			}
			else if (_updated)
			{
				updateChildCollections(false);
				doUpdate ();
				l = _lastUpdated;
			}
			else
			{
				updateChildCollections(false);				
				if (_updated)
				{
					doUpdate ();
					l = _lastUpdated;
				}
			}
		} catch (Exception e) {
			throw new CommitException (this, e);
		}
		updateChildCollections(true);
		if (l < _lastUpdated)
		{
			try {
				doUpdate ();
			} catch (Exception e) {
				throw new CommitException (this, e);
			}
		}
	}

	private void updateChildCollections(boolean afterParent) throws CommitException {
		if ( _childrenUpdated && !_deleted)
		{
			Iterator it = children.entrySet().iterator();
			while (it.hasNext())
			{
				Entry entry = (Entry) it.next();
				DataModelCollection list = (DataModelCollection) entry.getValue();
				if (! list.isDirty() )
				{
					if (list.updateBeforeParent()? ! afterParent : afterParent)
					{
						list.prepareCommit();
					}
				}
			}
		}
	}
	
	public void commit ()
	{
		if (!_transient)
		{
			boolean wasDeleted = _deleted;
			_new = false;
			_deleted = false;
			_updated = false;
			if ( ! wasDeleted)
			{
				if ( _childrenUpdated )
				{
					Iterator it = children.entrySet().iterator();
					while (it.hasNext())
					{
						Entry entry = (Entry) it.next();
						DataModelCollection list = (DataModelCollection) entry.getValue();
						if (! list.isDirty())
							list.commit();
					}
				}
				Iterator it = children.entrySet().iterator();
				while (it.hasNext())
				{
					Entry entry = (Entry) it.next();
					DataModelCollection list = (DataModelCollection) entry.getValue();
					list.refreshAfterCommit();
				}
			}
			_childrenUpdated = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#refresh()
	 */
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#refresh()
	 */
	public void refresh ()
	{
	    _childrenUpdated = false;
	    for (DataNodeCollection list: children.values())
	    {
			list.setDirty();
	    }
	}
	
	protected abstract void doInsert () throws Exception ;
	protected abstract void doUpdate () throws Exception;
	protected abstract void doDelete () throws Exception;

	protected void notifyParent ()
	{
		if (!_transient)
		{
			DataModelNode p = getParent();
			if (p != null && p != this)
				p.updateChildren();
			else if (getDataSource() instanceof Component)
				Events.postEvent("onChange", (Component) getDataSource(), null);
		}
			
	}
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#delete()
	 */
	public void delete ()
	{
		if ( ctx.getListModel() != null && !_deleted)
		{
			_deleted = true;
			ctx.getListModel().onDelete (this);
			notifyParent ();
		}
	}
	
	public void undelete ()
	{
		if (!_transient)
		{
			if ( ctx.getListModel() != null && _deleted)
			{
				_deleted = false;
				ctx.getListModel().onUndelete (this);
				notifyParent ();
			}
		}
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#update()
	 */
	public void update ()
	{
		_lastUpdated ++;
		if (!_new) _updated = true;
		if ( ctx.getListModel() != null)
		{
			ctx.getListModel().onUpdate (this);
			notifyParent ();
		}
	}
	
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#getParent()
	 */
	public DataModelCollection getContainer() {
		return ctx.getListModel();
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#isUpdatedChild()
	 */
	public boolean isCommitPending() {
		return _childrenUpdated;
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#updateChildren()
	 */
	public void updateChildren() {
		if (!_childrenUpdated)
		{
			_childrenUpdated = true;
			notifyParent ();
		}
	}
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#isDeleted()
	 */
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#isDeleted()
	 */
	public boolean isDeleted() {
		return _deleted;
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#isNew()
	 */
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#isNew()
	 */
	public boolean isNew() {
		return _new;
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.DataSource#isUpdated()
	 */
	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.UpdateableDataModel#isUpdated()
	 */
	public boolean isUpdated() {
		return _updated;
	}
	
	public void setDirty (boolean dirty)
	{
		if ( !dirty )
		{
			_new = _updated = _deleted = false;
		}
		else
		{
			if ( !_new ) _updated = true;
		}
	}
    /**
     * Does the specified mapped property contain a value for the specified
     * key value?
     *
     * @param name Name of the property to check
     * @param key Name of the key to check
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
	public boolean contains(String name, String value) {
		DataModelCollection listModel = getListModel(name);
		if (listModel == null)
			if (lazyMap == null)
		        throw new UnsupportedOperationException
		        	("WrapDynaBean does not support contains()");
			else 
				return lazyMap.contains(name, value);
		else
			return false;
	}

    /**
     * Return the value of an indexed property with the specified name.
     *
     * @param name Name of the property whose value is to be retrieved
     * @param index Index of the value to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not indexed
     * @exception IndexOutOfBoundsException if the specified index
     *  is outside the range of the underlying property
     * @exception NullPointerException if no array or List has been
     *  initialized for this property
     */
	public Object get(String name, int index) {
		DataModelCollection listModel = getListModel(name);
		if (listModel == null)
			if (lazyMap == null)
			{
		        try {
		            return PropertyUtils.getIndexedProperty(ctx.getData(), name, index);
		        } catch (IndexOutOfBoundsException e) {
		            throw e;
		        } catch (Throwable t) {
		            throw new IllegalArgumentException
		                    ("Property '" + name + "' has no indexed read method");
		        }
				
			}
			else
				return lazyMap.get(name,index);
		else
			return listModel.getDataModel(index);
	}

    /**
     * Return the value of a mapped property with the specified name,
     * or <code>null</code> if there is no value for the specified key.
     *
     * @param name Name of the property whose value is to be retrieved
     * @param key Key of the value to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not mapped
     */
	public Object get(String name, String key) {
		DataModelCollection obj  = getListModel(name);
		if ( obj == null) {
			if (lazyMap == null)
			{
		        try {
		            return PropertyUtils.getMappedProperty(ctx.getData(), name, key);
		        } catch (Throwable t) {
		            throw new IllegalArgumentException
		                    ("Property '" + name + "' has no mapped read method");
		        }
			}
			else
				return lazyMap.get(name, key);
		} else
			throw new IllegalArgumentException();
	}

    /**
     * Return the value of a simple property with the specified name.
     *
     * @param name Name of the property whose value is to be retrieved
     *
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     */
	public Object get(String name) {
		if (name.equals("instance"))
    		return ctx.getData();
		Object obj  = getListModel(name);
		if (obj == null)
			if (lazyMap == null)
			{
		        try {
		            return PropertyUtils.getSimpleProperty(ctx.getData(), name);
		        } catch (Throwable t) {
	        		throw new IllegalArgumentException
		                    ("Property '" + name + "' has no read method");
		        }
			}
			else 
				return lazyMap.get(name);
		else
			return obj;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#remove(java.lang.String, java.lang.String)
	 */
	public void remove(String name, String value) {
		Object obj  = getListModel(name);
		if (obj == null)

			if (lazyMap == null)
		        throw new UnsupportedOperationException
		        	("WrapDynaBean does not support remove()");
			else
				lazyMap.remove(name, value);
		else
			throw new IllegalArgumentException();
	}

    /**
     * Set the value of an indexed property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param index Index of the property to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not indexed
     * @exception IndexOutOfBoundsException if the specified index
     *  is outside the range of the underlying property
     */
	public void set(String name, int index, Object value) {
		DataModelCollection ulm  = getListModel(name);
		if ( ulm == null)
			if (lazyMap == null)
			{
			        try {
			            PropertyUtils.setIndexedProperty(ctx.getData(), name, index, value);
			        } catch (IndexOutOfBoundsException e) {
			            throw e;
			        } catch (Throwable t) {
			            throw new IllegalArgumentException
			                    ("Property '" + name + "' has no indexed write method");
			        }
			}
			else
				lazyMap.set (name, index,value);
		else if (value == null)
		{
			DataModelNode node = ulm.getDataModel(index);
			if (node != null)
				node.delete();
		}
		else
		{
			DataModelNode node = ulm.getDataModel(index);
			if (node instanceof DataNode)
			{
				DataNode dn = (DataNode) node;
				dn.ctx.setData(value);
				dn.update();
			}
			else
				throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.Object)
	 */
	public void set(String name, Object value) {
		Object obj  = getListModel(name);
		if ( obj == null)
			if (lazyMap == null)
			{
		        try {
		            PropertyUtils.setSimpleProperty(ctx.getData(), name, value);
		        } catch (Throwable t) {
		            throw new IllegalArgumentException
		                    ("Property '" + name + "' has no write method");
		        }
			}
			else
				lazyMap.set(name, value);
		else
			throw new IllegalArgumentException();
	}

    /**
     * Set the value of a mapped property with the specified name.
     *
     * @param name Name of the property whose value is to be set
     * @param key Key of the property to be set
     * @param value Value to which this property is to be set
     *
     * @exception ConversionException if the specified value cannot be
     *  converted to the type required for this property
     * @exception IllegalArgumentException if there is no property
     *  of the specified name
     * @exception IllegalArgumentException if the specified property
     *  exists, but is not mapped
     */
	public void set(String name, String key, Object value) {
		Object obj  = getListModel(name);
		if ( obj == null)
			if (lazyMap == null)
			{
			    try {
			        PropertyUtils.setMappedProperty(ctx.getData(), name, key, value);
			    } catch (Throwable t) {
			        throw new IllegalArgumentException
			                ("Property '" + name + "' has no mapped write method");
			    }
			}
			else
				lazyMap.set(name, key, value);
		else
			throw new IllegalArgumentException();
	}


	public DynaClass getDynaClass ()
	{
		if (lazyMap == null)
		{
			if ( dynaClass == null)
				dynaClass = new DataNodeDynaClass (this);
			return dynaClass;
		}
		else
			return lazyMap.getDynaClass();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.beanutils.DynaClass#newInstance()
	 */
	public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
		throw new InstantiationException();
	}

	/**
	 * @return Returns the parentDataNode.
	 */
	public DataModelNode getParent() {
		return ctx.getParent();
	}
	
	public DataSource getDataSource () {
		return ctx.getDataSource();
	}

	/**
	 * @return Returns the instance.
	 */
	public Object getInstance() {
		return ctx.getData();
	}

	public String getXPath ()
	{
		if (getParent() == null)
			return "";
		else
			return ctx.getParent().getXPath()+"/"+ctx.getXPath();
	}

	/**
	 * @return Returns the ctx.
	 */
	public DataContext getDataContext() {
		return ctx;
	}

	public void setNew() {
		_new = true;
	}


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the property descriptor for the specified property name.
     *
     * @param name Name of the property for which to retrieve the descriptor
     *
     * @exception IllegalArgumentException if this is not a valid property
     *  name for our DynaClass
     */
    protected DynaProperty getDynaProperty(String name) {

        DynaProperty descriptor = getDynaClass().getDynaProperty(name);
        if (descriptor == null) {
            throw new IllegalArgumentException
                    ("Invalid property name '" + name + "'");
        }
        return (descriptor);

    }
    

    /************************** MAP) Methods ******************************/
	public int size() {
		int size = children.size();
		size += getDynaClass().getDynaProperties().length;
		return size + 2;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean containsKey(Object key) {
		if (! (key instanceof String))
			return false;
		
		if ("instance".equals(key) || "parent".equals(key))
			return true;
		
		if (getListModel((String) key) != null)
			return true;
		if (lazyMap != null)
			return lazyMap.getDynaProperty((String) key) != null;
		else 
			return getDynaProperty((String) key) != null;
	}

	public boolean containsValue(Object value) {
		// I'm lazy to implement it
		return false;
	}

	public Object get(Object key) {
		if (! (key instanceof String))
			return false;
		
		if ("instance".equals(key))
			return getInstance();
		if ("parent".equals(key))
			return getParent();
		DataModelCollection lm = getListModel((String)key);
		if (lm != null)
			return lm;
		if (lazyMap != null)
			return lazyMap.get((String) key);
		else
			return get((String) key);
	}

	public Object put(Object key, Object value) {
		if (! (key instanceof String))
			return false;
		
		if ("instance".equals(key))
			throw new IllegalArgumentException();
		Object old = get(key);
		set((String) key, value);
		return old;
	}

	public Object remove(Object key) {
		if (! (key instanceof String))
			return null;
		
		Object old = get(key);
		set((String) key, null);
		return old;
	}

	public void putAll(Map m) {
		for (Object o: m.keySet())
			put (o, m.get(o));
	}

	public void clear() {
		throw new IllegalArgumentException();
	}

	public Set keySet() {
		throw new IllegalArgumentException();
	}

	public Collection values() {
		throw new IllegalArgumentException();
	}

	public Set entrySet() {
		throw new IllegalArgumentException();
	}


}

class DataNodeDynaClass implements DynaClass, Serializable
{
	DataNode master;
	transient DynaClass wrappedDynaClass;
	
	DynaClass getWrappedDynaClass ()
	{
		if (wrappedDynaClass == null)
			wrappedDynaClass = WrapDynaClass.createDynaClass(master.getInstance().getClass());
		return wrappedDynaClass;
			
	}
	DataNodeDynaClass (DataNode master)
	{
		this.master = master;
	}

	public String getName() {
		return getWrappedDynaClass().getName();
	}
	public DynaProperty getDynaProperty(String name) {
		DynaProperty prop = (DynaProperty) master.dynaPropertiesMap.get(name);
		if ( prop != null)
			return prop;
		else
			return getWrappedDynaClass().getDynaProperty(name);
	}
	
	public DynaProperty[] getDynaProperties() {
		Vector<DynaProperty> v = new Vector<DynaProperty>(master.dynaProperties);
		DynaProperty d [] = getWrappedDynaClass().getDynaProperties();
		for (int i = 0; i < d.length; i++)
			v.add (d[i]);
		return (DynaProperty[]) v.toArray(new DynaProperty[0]);
	}
	
	public DynaBean newInstance() throws IllegalAccessException, InstantiationException {
		throw new InstantiationException();
	}
	

	public String getParentProperty() {
		return null;
	}
	
	public String getIdProperty () {
		return null;	
	}
	
	public String getChildProperty() {
		return null;
	}
	
	public Object loadParentObject() throws Exception {
		return null;
	}

}

