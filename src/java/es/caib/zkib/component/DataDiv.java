/* Grid.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Oct 25 15:40:35     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.RowRendererExt;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;
import org.zkoss.zul.impl.XulElement;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataDiv extends XulElement implements AfterCompose, BindContext, XPathSubscriber {
	Component templateElement = null;
	boolean composed = false;
	
	@Override
	public Object clone() {
		final DataDiv clone = (DataDiv) super.clone();
		clone.binder = new CollectionBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		clone._dataListener = null;
		clone.addEventListener("onApplyDatapath", new EventListener() {
			public void onEvent(Event event) throws Exception {
				clone.applyDataPath();
			}
		});
		Events.postEvent(new Event("onApplyDatapath", clone));
		return clone;
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CollectionBinder binder = new CollectionBinder(this);

	private static final Log log = Log.lookup(DataDiv.class);

	private ListModel _model;
	private transient ListDataListener _dataListener;

	public DataDiv() {
		init();
	}

	private void init() {
	}
	
	//-- ListModel dependent codes --//
	/** Returns the list model associated with this grid, or null
	 * if this grid is not associated with any list data model.
	 */
	public ListModel getModel() {
		return _model;
	}
	/** Sets the list model associated with this grid.
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
				}
				initDataListener();
				_model = model;
				_model.addListDataListener(_dataListener);
			}

			if (composed) {
				invalidate();
	
				syncModel(); //create rows if necessary
			}
		} else if (_model != null) {
			_model.removeListDataListener(_dataListener);
			_model = null;
			if (composed) {
				Iterator<Component> it = getChildren().iterator();
				while (it.hasNext()) { it.next(); it.remove();}
				invalidate();
			}
		}
	}
	private void initDataListener() {
		if (_dataListener == null)
			_dataListener = new ListDataListener() {
				public void onChange(ListDataEvent event) {
					onListDataChange(event);
				}
			};
	}

	
	/** Creates an new and unloaded listitem. */
	protected Component newRow (int j, Component before) {
//		Component r = getFirstChild();
		Component r = templateElement;
		if (r == null)
			return null;
		
		Component c = (Component) r.clone ();
		if (getModel() != null && getModel() instanceof ModelProxy)
		{
			String xPath = ((ModelProxy) getModel()).getBind(j);
			SingletonBinder binder = new SingletonBinder(c);
			binder.setDataPath(xPath);
			c.setAttribute(BindContext.BINDCTX_ATTRIBUTE, binder, Component.COMPONENT_SCOPE);
		}
		insertBefore(c, before);
		Events.postEvent("onNewRow", this, c);
		return c;
	}

	/** Synchronizes the grid to be consistent with the specified model.
	 *
	 * @param min the lower index that a range of invalidated rows
	 * @param max the higher index that a range of invalidated rows
	 */
	private void syncModel() {
		Iterator<Component> it = getChildren().iterator();
		while (it.hasNext()) { it.next(); it.remove();}

		final int newsz = _model.getSize();
		for ( int i = 0; i < newsz; i++)
			newRow (i, null);
//		invalidate();
	}
	
	/** Handles when the list model's content changed.
	 */
	private void onListDataChange(ListDataEvent event) {
		//when this is called _model is never null
		final int newsz = _model.getSize(), oldsz = getChildren().size();
		int min = event.getIndex0(), max = event.getIndex1();
		if (min < 0) min = 0;

		switch (event.getType()) {
		case ListDataEvent.INTERVAL_ADDED:
			if (max < 0) max = newsz - 1;
			if ((max - min + 1) != (newsz - oldsz)) {
				log.warning("Conflict event: number of added items not matched: "+event);
				break; //handle it as CONTENTS_CHANGED
			}
			final Component before = min >= getChildren().size() ? null : (Component) getChildren().get(min);
			for (int j = min; j <= max; ++j)
			{
				
				newRow(j, before);
			}
			break;

		case ListDataEvent.INTERVAL_REMOVED:
			if (max < 0) max = oldsz - 1;
			if ((max - min + 1) != (oldsz - newsz)) {
				log.warning("Conflict event: number of removed items not matched: "+event);
				break; //handle it as CONTENTS_CHANGED
			}
			for (int j = min; j <= max; ++j)
				((Component)getChildren().get(min+0)).detach(); //detach and remove
			break;
		}
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

	public List<Component> getRenderedChildren ()
	{
		LinkedList<Component> rc = new LinkedList<Component>();
		Iterator<Component> it = getChildren().iterator();
//		if (it.hasNext()) it.next();
		while (it.hasNext())
			rc.add (it.next());
		return rc;
	}

	public Component getRenderedChild ( int i)
	{
		if (i >= getChildren().size() - 1 || i < 0)
			return null;
		else
			return (Component) getChildren().get(i+1);
	}

	public void afterCompose() {
		if (templateElement == null )
		{
			if (getChildren().size() > 0)
			{
				templateElement = getFirstChild();
				templateElement.setParent(null);
			}
		}
		composed = true;
		if (_model != null)
			syncModel();
	}

	public void setPage(Page page) {
		super.setPage(page);
		if (page == null && _model != null) {
			_model.removeListDataListener(_dataListener);
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

	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	public String getXPath() {
		return binder.getXPath();
	}

	public void onUpdate(XPathEvent event) {
		if (event instanceof XPathRerunEvent)
		{
			applyDataPath ();
		}
	}

	@Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final String uuid = getUuid();		

		wh.write("<div id=\"").write(uuid).write("\" ")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write(">")
			.writeComponents(getRenderedChildren())
			.write("</div>");
	}
}
