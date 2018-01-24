package es.caib.zkib.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.list.DataListItemRenderer;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datasource.ChildDataSourceImpl;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathNotFoundException;
import es.caib.zkib.jxpath.Pointer;
import es.caib.zkib.jxpath.Variables;

public class DataListbox extends Listbox implements XPathSubscriber,
        BindContext, DataSource, AfterCompose {
	String dataPath;
    /**
     * 
     */
    private static final long serialVersionUID = 7972552691186027886L;
    SingletonBinder nofilterCollectionBinder = new SingletonBinder(this);
    CollectionBinder collectionBinder = new CollectionBinder(this);
    SingletonBinder valueBinder = new SingletonBinder(this);
    ChildDataSourceImpl dsImpl = null;
    boolean autocommit = true;
    boolean _updateValueBinder = true;
    
    private boolean noSizable = false; //per defecte sizable (capçalera)

    private MasterListItem _masterListItem;
	private String selectedItemXPath;
	private String additionalFilter;

    public DataListbox() {
        super();
    }
    
    public void onCreate ()
    {
        syncSelectedItem();
    }

    public String getBind() {
        return valueBinder.getDataPath();
    }

    public void setBind(String bind) {
        valueBinder.setDataPath(bind);
        if (bind != null)
        	enableOnSelectListener();
        // Events.postEvent("onInitRender", this, null);
    }

    public String getDataPath() {
        return dataPath;
    }
    
    public String getSelectedItemXPath () {
    	return selectedItemXPath;
    }

    public void setDataPath(String bind) {

    	dataPath = bind;
        applyDataPath();
        if (bind != null)
        	enableOnSelectListener();
    }

    /**
     * 
     */
    private void applyDataPath() {
        ListModel oldListModel = getModel();
        if (oldListModel != null && oldListModel instanceof ModelProxy)
            ((ModelProxy) oldListModel).unsubscribe();

        additionalFilter = getAdditionalFilter ();
        if (additionalFilter == null || additionalFilter.length() == 0)
        {
        	nofilterCollectionBinder.setDataPath(null);
        	collectionBinder.setDataPath(dataPath);
        }
        else
        {
        	String newdp = dataPath+additionalFilter;
        	nofilterCollectionBinder.setDataPath(dataPath);
        	collectionBinder.setDataPath(newdp);
        }
        ListModel lm = collectionBinder.createModel();
        setModel(lm);

        if (lm == null) {
            if (dsImpl != null)
                dsImpl.setRootXPath(null);
        } else if (getPage () != null) {
            if (dsImpl == null)
                dsImpl = new ChildDataSourceImpl();

            dsImpl.setDataSource(collectionBinder.getDataSource());
            dsImpl.setRootXPath(collectionBinder.getModelXPath());
            if (lm instanceof ModelProxy) {
                selectedItemXPath = ((ModelProxy) getModel())
                        .getBind(getSelectedIndex());
                dsImpl.setXPath(selectedItemXPath);
            }
        }
    }

    
    private void findFilters (Component c, List<HeaderFilter> filters)
    {
    	if (c instanceof HeaderFilter)
    	{
    		filters.add((HeaderFilter) c);
    	}
    	else
    	{
    		for (Component child: (Collection<Component>)c.getChildren())
    		{
    			findFilters(child, filters);
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
	private String getAdditionalFilter() {
    	StringBuffer filter = new StringBuffer();
    	List<HeaderFilter> selectors = new LinkedList<HeaderFilter>();
		findFilters (this, selectors);
    	for (HeaderFilter lbf: selectors)
    	{
   			lbf.appendFilter (filter);
    	}
    	return filter.toString();
	}

    @SuppressWarnings("unchecked")
	public void updateFilters() {
    	// Search selectors
    	List<HeaderFilter> selectors = new LinkedList<HeaderFilter>();
		findFilters (this, selectors);
		if (selectors.isEmpty())
			return;
		HeaderFilter selectorsArray[] = selectors.toArray(new HeaderFilter[selectors.size()]);
		HashSet<String> values[] = new HashSet[selectors.size()];
		for (int i = 0; i < selectors.size(); i++)
			values[i] = new HashSet<String>();
		
		// Createa data context
		JXPathContext ctx;
		String path ;
		if (nofilterCollectionBinder.getDataPath() == null)
			ctx = collectionBinder.getJXPathContext();
		else
			ctx = nofilterCollectionBinder.getJXPathContext();
		if (ctx == null)
			return ;
		// Search data
		for ( Iterator it = ctx.iteratePointers("/"); it.hasNext();)
		{
			Pointer p = (Pointer) it.next();
			for (int i = 0; i < selectors.size(); i++)
			{
				HeaderFilter selector = selectorsArray[i];
				try {
					Object value = ctx.getRelativeContext(p).getValue( selector.getBind());
					if (value != null)
					{
						values[i].add(value.toString());
					}
				} catch (JXPathNotFoundException e) {
					// Ignore temporary invalid paths
				}
			}
		}
		// Update filters
		for (int i = 0;i < selectorsArray.length; i++)
		{
			HeaderFilter selector = selectorsArray[i];
			if (selector instanceof ListboxFilter)
			{
				ListboxFilter listboxSelector = (ListboxFilter) selector;
				List<Listitem> items = listboxSelector.getItems();
				items.clear();
				items.add(new Listitem ("-", null));
				for (String value :values[i] ) {
					items.add (new Listitem (value, value));
				}
				listboxSelector.setSelectedIndex(0);
			}
		}
	}

    public DataSource getDataSource() {
        return collectionBinder.getDataSource();
    }

    public void sendEvent(XPathEvent event) {
        dsImpl.sendEvent(event);
    }

    /** Esto recibe los eventos asociados al valor resultado, no al modelo */
    public void onUpdate(XPathEvent event) {
        if (event instanceof XPathValueEvent) {
            syncSelectedItem();
        }
        if (event instanceof XPathCollectionEvent) {
            XPathCollectionEvent e = (XPathCollectionEvent) event;
            if (e.getType() == XPathCollectionEvent.FOCUSNODE
                    && getModel() instanceof ModelProxy) {
                ModelProxy mp = (ModelProxy) getModel();
                int i = mp.getPosition(e.getIndex());
                if (i >= 0) {
                    setSelectedIndex(i);
                }
            }
            if (e.getType() == XPathCollectionEvent.RECREATED
                    && getModel() instanceof ModelProxy) {
                setSelectedIndex(-1);
            }
        }
        if (event instanceof XPathRerunEvent) {
            boolean oldValue = _updateValueBinder;
            _updateValueBinder = false;
            applyDataPath();
            setSelectedIndex(-1);
            updateFilters();
            valueBinder.setDataPath(valueBinder.getDataPath());
            syncSelectedItem();
            _updateValueBinder = oldValue;
        }

    }

    /**
     * 
     */
    private void syncSelectedItem() {
        Object value = valueBinder.getValue();
        if ("label".equals(getMold()))
        	invalidate();
        Object instance = value instanceof DataNode ? ((DataNode)value).getInstance(): value;
        if (value != null) {
            Iterator it = getItems().iterator();
            while (it.hasNext()) {
                Listitem item = (Listitem) it.next();
                if (item.getValue() == null) {
                    renderItem(item);
                }
                Object value2 = item.getValue();
                if (value2 != null)
                {
	                Object instance2 = value2 instanceof DataNode ? ((DataNode)value2).getInstance(): value2;
	                if (value.equals(value2) ||
	                		instance.equals(instance2)) {
	                    boolean oldValue = _updateValueBinder;
	                    _updateValueBinder = false;
	                    setSelectedIndex(item.getIndex());
	                    _updateValueBinder = oldValue;
	                }
                }
            }
        }
    }

    public JXPathContext getJXPathContext() {
        if (dsImpl == null)
            return null;
        else
            return dsImpl.getJXPathContext();
    }

    EventListener onSelectListener = null;

    public void subscribeToExpression(String path, XPathSubscriber subscriber) {
        dsImpl.subscribeToExpression(path, subscriber);
        enableOnSelectListener();
    }

	private void enableOnSelectListener() {
		if (onSelectListener == null) {
            onSelectListener = new OnSelectListener();

            this.addEventListener("onSelect", onSelectListener);

        }
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#smartUpdate(java.lang.String,
     *      java.lang.String)
     */
    public void smartUpdate(String attr, String value) {
        if ("selectedIndex".equals(attr) || "select".equals(attr)) {
            int i = getSelectedIndex();

            if (getModel() != null && getModel() instanceof ModelProxy
                    && dsImpl != null) {
                selectedItemXPath = ((ModelProxy) getModel()).getBind(i);
                dsImpl.setXPath(selectedItemXPath);
            }

            if ( _updateValueBinder)
            {
                if (i < 0)
                    valueBinder.setValue(null);
                else {
                    Listitem item = getItemAtIndex(i);
                    String label = item.getLabel();
                    valueBinder.setValue(item.getValue());
                }
            }
        }
        super.smartUpdate(attr, value);
    }

    public MasterListItem getMasterListItem() {
        return _masterListItem;
    }

    public boolean insertBefore(Component newChild, Component refChild) {
        if (newChild instanceof MasterListItem) {
            _masterListItem = (MasterListItem) newChild;
            _masterListItem.setParent(null);
            _masterListItem.setTheListbox(this);
            this.setItemRenderer(new DataListItemRenderer(this));
            return false;
		} else if (newChild instanceof Listhead) {
			// Hacemos que la cabecera sea siempre ajustable
			// Alejandro Usero Ruiz - 12/07/2011
			if (!isNoSizable())
				((Listhead) newChild).setSizable(true);
			return super.insertBefore(newChild, refChild);
		} else
			return super.insertBefore(newChild, refChild);
    }

    public String getXPath() {
        return collectionBinder.getModelXPath();
    }

    public void unsubscribeToExpression(String xpath, XPathSubscriber subscriber) {
        dsImpl.unsubscribeToExpression(xpath, subscriber);
    }

    public void autocommit() {
        if (isAutocommit()) {
            try {
                commit();
            } catch (UnsupportedOperationException e) {
                // Ignorar
            }
        }
    }

    public void commit() throws UiException {
        try {
            if (collectionBinder != null
                    && collectionBinder.getDataSource() != null)
                collectionBinder.getDataSource().commit();
        } catch (CommitException e) {
            throw new UiException(e.getCause().toString());
        }
    }

    /**
     * @return Returns the autocommit.
     */
    public boolean isAutocommit() {
        return autocommit;
    }

    /**
     * @param autocommit
     *                The autocommit to set.
     */
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    public void addNew() {
        if (!(getModel() instanceof ModelProxy))
            throw new UiException(
                    "Not allowed to creaete records without a model");

        if (autocommit)
            commit();

        try {
            ModelProxy dm = (ModelProxy) getModel();
            int i = dm.newInstance();
            Listitem item = getItemAtIndex(i);
            renderItem(item);
            setSelectedIndex(i);
        } catch (Exception e) {
            throw new UiException(e);
        }
    }

    public void delete() {
        ListModel lm = getModel();
        if (lm == null || !(lm instanceof ModelProxy))
            throw new UiException(
                    "Not allowed to delete records without a model");

        if (autocommit)
            commit();

        if (getSelectedIndex() >= 0) {
            Object obj = lm.getElementAt(getSelectedIndex());
            if (!(obj instanceof DataModelNode))
                throw new UiException("Unable to delete object " + obj);
            ((DataModelNode) obj).delete();
            if (autocommit)
                commit();
            setSelectedIndex(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see es.caib.seycon.net.web.zul.datasource.ChildDataSourceImpl#getVariables()
     */
    public Variables getVariables() {
        return dsImpl.getVariables();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#clearSelection()
     */
    public void clearSelection() {
        super.clearSelection();
        selectedItemXPath = null;
        if (dsImpl != null)
            dsImpl.setXPath(null);
    }

    public boolean isCommitPending() {
        if (dsImpl == null)
            return false;
        else
            return dsImpl.isCommitPending();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#onInitRender()
     */
    public void onInitRender() {
        super.onInitRender();
        syncSelectedItem();
    }

    public void setPage(Page page) {
        super.setPage(page);
        valueBinder.setPage(page);
        collectionBinder.setPage(page);
    }

    public void setParent(Component parent) {
        super.setParent(parent);
        valueBinder.setParent(parent);
        collectionBinder.setParent(parent);
    }

    public Object clone() {
        DataListbox clone = (DataListbox) super.clone();
        clone.valueBinder = new SingletonBinder(clone);
        clone.collectionBinder = new CollectionBinder(clone);
        clone.setDataPath(collectionBinder.getDataPath());
        clone.setBind(valueBinder.getDataPath());
        return clone;
    }

    private final class OnSelectListener implements EventListener, Serializable {
		private static final long serialVersionUID = 1L;

		public boolean isAsap() {
		    return true;
		}

		public void onEvent(org.zkoss.zk.ui.event.Event arg0) {// NOTHING
		                                                        // TO DO
		}
	}

	protected class ExtraCtrl extends Listbox.ExtraCtrl {

        // -- Selectable --//
        public void selectItemsByClient(Set selItems) {
            autocommit();

            super.selectItemsByClient(selItems);
        }

    }

	public boolean isNoSizable() {
		return noSizable;
	}

	public void setNoSizable(boolean noSizable) {
		this.noSizable = noSizable;
	}

	public String getRootPath() {
		return dsImpl.getRootPath();
	}
	
	public void onUpdateFilterSelection ()
	{
        String newAdditionalFilter = getAdditionalFilter ();
        if ( ! newAdditionalFilter.equals(additionalFilter))
        	applyDataPath();
	}

	public void afterCompose() {
		updateFilters();
	}
}
