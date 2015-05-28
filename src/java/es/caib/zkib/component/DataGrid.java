package es.caib.zkib.component;

import java.util.ArrayList;
import java.util.Iterator;

import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataGrid extends Grid implements BindContext, XPathSubscriber {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4012294145045750731L;
	private CollectionBinder binder = new CollectionBinder(this);
	private ListDataListener _dataListener;
	private static final Log log = Log.lookup(DataGrid.class);
	private boolean _renderInitiated = false;

	ListModel _model;
	
	private boolean noSizable = false; //per defecte sizable

	private DataRow _masterRow;
	private EventListener onInitListener;

	public DataGrid() {
		super();
	}



	public void setPage(Page page) {
		super.setPage(page);
		if (page == null && _model != null) {
			_model.removeListDataListener(_dataListener);
			removeEventListener("onRenderDataGrid", onInitListener);
			_model = null;
		}
		binder.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		if (parent == null && _model != null) {
			_model.removeListDataListener(_dataListener);
			_model = null;
			binder.setParent(parent);
		} else {
			binder.setParent(parent);
			applyDataPath();
		}
	}

	/** Returns the list model associated with this listbox, or null
	 * if this listbox is not associated with any list data model.
	 */
	public ListModel getModel() {
		return _model;
	}
	/** Sets the list model associated with this listbox.
	 * If a non-null model is assigned, no matter whether it is the same as
	 * the previous, it will always cause re-render.
	 *
	 * @param model the list model to associate, or null to dis-associate
	 * any previous model.
	 * @exception UiException if failed to initialize with the model
	 */
	public void setModel(ListModel model) {
		if (model != null) {
			if (_model != model) {
				if (_model != null) {
					_model.removeListDataListener(_dataListener);
				} else {
					smartUpdate("zk_model", "true");
				}

				_model = model;

				if (_dataListener == null) {
					_dataListener = new ListDataListener() {
						public void onChange(ListDataEvent event) {
							onListDataChange(event);
						}
					};
				}
				_model.addListDataListener(_dataListener);
			}

			//Always syncModel because it is easier for user to enfore reload
			if (onInitListener == null)
			{
				onInitListener = new EventListener () {
					public boolean isAsap() {return false;};
					public void onEvent(org.zkoss.zk.ui.event.Event arg0) {
						syncModel();
					};
				};
				addEventListener("onRenderDataGrid", onInitListener);
			}
			Events.postEvent("onRenderDataGrid", this, null);
			//Since user might setModel and setRender separately or repeatedly,
			//we don't handle it right now until the event processing phase
			//such that we won't render the same set of data twice
			//--
			//For better performance, we shall load the first few row now
			//(to save a roundtrip)
		} else if (_model != null) {
			_model.removeListDataListener(_dataListener);
			_model = null;
			clear();
			smartUpdate("zk_model", null);
		}
	}

	/** Synchronizes the listbox to be consistent with the specified model.
	 * @param min the lower index that a range of invalidated items
	 * @param max the higher index that a range of invalidated items
	 */
	private void syncModel() {
		if (_model == null || getRows() == null)
			return;
		final int newsz = _model.getSize();
		
		final int oldsz = getRows().getChildren().size();

		for (int j = oldsz - 1; j >= 0; --j)
			getRowAtIndex(j).detach(); //detach and remove

		for (int j = 0; j < newsz; ++j)
			newRow(j);
	}
	
	
	/** Creates an new and unloaded listitem. */
	private DataRow newRow (int j) {
		MasterRow r = (MasterRow) _masterRow.clone();
		r.unlock ();
		r.setParent(getRows ());
		if (getModel() != null && getModel() instanceof ModelProxy)
		{
			r.setDataPath(((ModelProxy) getModel()).getBind(j));
		}
		Events.postEvent("onNewRow", this, r);
		return r;
	}
	
	
	/** Handles when the list model's content changed.
	 */
	private void onListDataChange(ListDataEvent event) {
		//when this is called _model is never null
		final int newsz = _model.getSize(), oldsz = getRowCount();
		int min = event.getIndex0(), max = event.getIndex1();
		if (min < 0) min = 0;

		switch (event.getType()) {
		case ListDataEvent.INTERVAL_ADDED:
			if (max < 0) max = newsz - 1;
			if ((max - min + 1) != (newsz - oldsz)) {
				log.warning("Conflict event: number of added items not matched: "+event);
				break; //handle it as CONTENTS_CHANGED
			}
			final Row before = min < oldsz ? getRowAtIndex(min): null;
			for (int j = min; j <= max; ++j)
			{
				
				getRows().insertBefore(newRow(j), before);
			}
			break;

		case ListDataEvent.INTERVAL_REMOVED:
			if (max < 0) max = oldsz - 1;
			if ((max - min + 1) != (oldsz - newsz)) {
				log.warning("Conflict event: number of removed items not matched: "+event);
				break; //handle it as CONTENTS_CHANGED
			}
			for (int j = min; j <= max; ++j)
				getRowAtIndex(min).detach(); //detach and remove
			break;
		}

		if (getPage() != null)
			invalidate ();
	}

	private int getRowCount() {
		return getRows().getChildren().size();
	}


	protected void setMasterRow (DataRow r)
	{
		_masterRow = r;
		if (getRows () == null)
		{
			Rows rows = new Rows ();
			rows.setParent(this);
		}
		// This parent will be removed on aftercompose
		_masterRow.setParent( getRows());
	}

	private Row getRowAtIndex(int i) {
		return (Row) getRows().getChildren().get(i);
	}



	/** Clears all child items.
	 */
	public void clear() {
		if (!getRows().getChildren().isEmpty()) {
			for (Iterator it = new ArrayList(getRows().getChildren()).iterator(); it.hasNext();)
				((Component)it.next()).detach(); //detach and remove
		}
	}



	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#getBind()
	 */
	public String getDataPath() {
		return binder.getDataPath();
	}



	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#getDataSource()
	 */
	public DataSource getDataSource() {
		return binder.getDataSource();
	}



	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#setBind(java.lang.String)
	 */
	public void setDataPath(String bind) {
		binder.setDataPath(bind);
		applyDataPath();
	}


	/**
	 * 
	 */
	private void applyDataPath() {
		setModel ( binder.createModel ()); 
	}



	public String getXPath() {
		return binder.getXPath();
	}


	/** Esto recibe los eventos asociados al valor resultado, no al modelo */
	public void onUpdate (XPathEvent event) {
		if (event instanceof XPathRerunEvent)
		{
			applyDataPath ();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.Listbox#onInitRender()
	 */
	public void doInitRender() {
		if (! _renderInitiated)
		{
			_renderInitiated = true;
			syncModel();
		}
	}

	public Object clone() {
		final DataGrid clone = (DataGrid) super.clone();
		clone.binder = new CollectionBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		clone._dataListener = null;
		clone.onInitListener = null;
		clone.addEventListener("onApplyDatapath", new EventListener() {
			
			public void onEvent(Event event) throws Exception {
				clone.applyDataPath();
			}
		});
		Events.postEvent(new Event("onApplyDatapath", clone));
		return clone;
	}



	public boolean insertBefore(Component newChild, Component refChild) {
		if (newChild instanceof Columns) {
			// Hacemos que la cabecera ses siempre ajustable
			// Alejandro Usero Ruiz - 12/07/2011
			if (!isNoSizable())
				((Columns) newChild).setSizable(true);
			return super.insertBefore(newChild, refChild);
		} else {
			return super.insertBefore(newChild, refChild);
		}
	}


	public boolean isNoSizable() {
		return noSizable;
	}

	public void setNoSizable(boolean noSizable) {
		this.noSizable = noSizable;
	}
	
}
