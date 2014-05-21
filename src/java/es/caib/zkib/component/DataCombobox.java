package es.caib.zkib.component;

import java.util.Iterator;
import java.util.Set;


import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.impl.XulElement;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.list.DataComboitemRenderer;
import es.caib.zkib.binder.list.DataListItemRenderer;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datasource.ChildDataSourceImpl;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Variables;

public class DataCombobox extends org.zkoss.zul.Combobox implements XPathSubscriber, BindContext {

	private void updateValueBinder() {
		if (valueBinder.isValid())
		{
			Object value = getValue();
			if (value != null)
			{
				Iterator it = getItems().iterator();
				while (it.hasNext())
				{
					Comboitem item = (Comboitem) it.next();
					if (value.equals(item.getLabel()))
					{
						value = item.getValue();
						break;
					}
				}
			}
			if (! value.equals(valueBinder.getValue()))
				valueBinder.setValue(value);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7972552691186027886L;
	CollectionBinder collectionBinder = new CollectionBinder (this);
	SingletonBinder valueBinder = new SingletonBinder(this);
	boolean autocommit = true;

	private MasterComboItem _masterComboItem;
	
	
	public DataCombobox() {
		super();
	}

	public String getBind() {
		return valueBinder.getDataPath();
	}

	public void setBind(String bind) {
		valueBinder.setDataPath(bind);
	}

	public String getDataPath() {
		return collectionBinder.getDataPath();
	}

	public void setDataPath(String bind) {
		collectionBinder.setDataPath (bind);
		applyDataPath();
	}

	// Gestión a lo listbox
	/**
	 * 
	 */
	private void applyDataPath() {
		ListModel oldListModel = getModel ();
		if ( oldListModel != null && oldListModel instanceof ModelProxy)
			((ModelProxy) oldListModel).unsubscribe ();
		
		ListModel lm = collectionBinder.createModel(); 
		setModel ( lm );
	}

	public DataSource getDataSource() {
		return collectionBinder.getDataSource();
	}


	/** Esto recibe los eventos asociados al valor resultado, no al modelo */
	public void onUpdate (XPathEvent event) {
		if (event instanceof XPathValueEvent)
		{
			updateSelectedItemFromValueBinder();
		}
		if (event instanceof XPathCollectionEvent)
		{
			XPathCollectionEvent e = (XPathCollectionEvent) event;
			if ( e.getType() == XPathCollectionEvent.FOCUSNODE && getModel() instanceof ModelProxy)
			{
				ModelProxy mp = (ModelProxy) getModel();
				int i = mp.getPosition(e.getIndex());
				if ( i >= 0 )
				{
					setSelectedIndex(i);
				}
			}
		}
		if (event instanceof XPathRerunEvent)
		{
			applyDataPath ();
			updateSelectedItemFromValueBinder();
		}
		
	}

	/**
	 * 
	 */
	private void updateSelectedItemFromValueBinder() {
		if (valueBinder.isValid())
		{
			Object value = valueBinder.getValue();
			String label = value == null? null: value.toString();
			if (value != null)
			{
				Iterator it = getItems().iterator();
				while (it.hasNext())
				{
					Comboitem item = (Comboitem) it.next();
					if (value.equals(item.getValue()))
					{
						setSelectedItem(item);
						return;
					}
				}
			}
			super.setValue(label);
		}
	}


	private boolean duringOnUpdate;


	public MasterComboItem getMasterComboItem() {
		return _masterComboItem;
	}

	public boolean insertBefore(Component newChild, Component refChild) {
		if (newChild instanceof MasterComboItem)
		{
			_masterComboItem = (MasterComboItem) newChild;
			_masterComboItem.setParent(null);
			this.setItemRenderer(new DataComboitemRenderer (this));
			return false;
		}
		else
			return super.insertBefore(newChild, refChild);
	}

	public String getXPath() {
		return collectionBinder.getModelXPath();
	}

	public void autocommit ()
	{
		if (isAutocommit()) {
			try {
				commit ();
			} catch (UnsupportedOperationException e) {
				// Ignorar
			}
		}
	}
	
	public void commit() throws UiException {
		try {
			if (collectionBinder != null && collectionBinder.getDataSource() != null)
				collectionBinder.getDataSource().commit();
		} catch (CommitException e) {
			throw new UiException (e.getCause().toString());
		}
	}

	/**
	 * @return Returns the autocommit.
	 */
	public boolean isAutocommit() {
		return autocommit;
	}

	/**
	 * @param autocommit The autocommit to set.
	 */
	public void setAutocommit(boolean autocommit) {
		this.autocommit = autocommit;
	}

	
	public void addNew ()
	{
		if (! (getModel() instanceof ModelProxy))
			throw new UiException ("Not allowed to creaete records without a model");

		if (autocommit) commit ();
		
		
		try {
			ModelProxy dm = (ModelProxy) getModel();
			int i = dm.newInstance();
			setSelectedIndex(i);
		} catch (Exception e) {
			throw new UiException (e);
		}
	}

	public void delete ()
	{
		ListModel lm = getModel ();
		if ( lm == null || ! ( lm instanceof ModelProxy))
			throw new UiException ("Not allowed to delete records without a model");

		
		
		if (autocommit) commit ();
		
		if (getSelectedIndex() >= 0)
		{
			Object obj = lm.getElementAt(getSelectedIndex());
			if ( ! (obj instanceof DataModelNode))
				throw new UiException ("Unable to delete object "+obj);
			( (DataModelNode) obj).delete();
			if (autocommit) commit ();
			setSelectedIndex( -1 );
		}
	}


	/* (non-Javadoc)
	 * @see org.zkoss.zul.Listbox#onInitRender()
	 */
	public void onInitRender(Event data) {
		super.onInitRender(data);
		updateSelectedItemFromValueBinder();
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
		DataCombobox clone = (DataCombobox) super.clone();
		clone.valueBinder = new SingletonBinder (clone);
		clone.collectionBinder = new CollectionBinder (clone);
		clone.setDataPath(collectionBinder.getDataPath());
		clone.setBind(valueBinder.getDataPath());
		return clone;
	}
	
	protected class ExtraCtrl extends Combobox.ExtraCtrl
	{
		
		//-- Selectable --//
		public void selectItemsByClient(Set selItems) {
			autocommit();
			
			super.selectItemsByClient(selItems);
		}

	}
	// Gestión a lo text-box
	public void undo(){
		valueBinder.setOldValue();
	}
	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#setBind(java.lang.String)
	 */

	public void setText(String value) throws WrongValueException {
		super.setText(value);
		updateValueBinder ();
	}

	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#setBind(java.lang.String)9
	 */

	public void setValue(String value) throws WrongValueException {
		super.setValue(value);
		updateValueBinder ();
	}

	public boolean effectiveDisabled = false;
	/* (non-Javadoc)
	 * @see org.zkoss.zul.impl.InputElement#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		effectiveDisabled = disabled;
		super.setDisabled(disabled);
	}
	
	public void setOldValue() {
		valueBinder.setOldValue();
	}


}
