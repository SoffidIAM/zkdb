package es.caib.zkib.binder;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import es.caib.zkib.binder.list.FullModelProxy;
import es.caib.zkib.binder.list.ListModelProxy;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.binder.list.PartialModelProxy;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathException;
import es.caib.zkib.jxpath.Pointer;

public class CollectionBinder extends AbstractBinder  {
	private List<Pointer> items;
	private DataModelCollection _model;
	private String _xPathSubscription = null;
	private JXPathContext _jxpContext;
	private Pointer _modelPointer;
	boolean partialModel = false;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CollectionBinder[Component="+
			getComponent().getDefinition().getName()+":"+getComponent().getId()+
			",Path="+getXPath()+"]";
	}

	public CollectionBinder(Component component) {
		super(component);
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.BinderImplementation#parsePath()
	 */
	protected void parsePath() {
		String path = getXPath();
		
		// Caso a => Lista indefinida de objetos
		_model = null;
		_modelPointer = null;
		_xPathSubscription = path;

		if (path == null) {
			items = new Vector<Pointer> ();
		} else if (path.endsWith("]")) {
			parsePartialModel(path);
		} else {	// Caso B => un solo objeto (posiblemente lista)
			parseFullModel(path);
		}		
	}

	/**
	 * @param path
	 */
	private void parseFullModel(String path) {
		items = null;
		Object value = null;
		Pointer p = null;
		try {
			p = getDataSource().getJXPathContext().getPointer(path);
			value = p.getValue();
		} catch (JXPathException e) {
		}
		if (value != null && value instanceof DataModelCollection)
		{
			_modelPointer = p;
			_model = (DataModelCollection) value;
			_xPathSubscription = _modelPointer.asPath();
			parseNoModel (path);
			partialModel = false;
		}
		else
		{
			parseNoModel (path);
		}
	}

	/**
	 * @param path
	 */
	private void parsePartialModel(String path) {
		// Buscar el padre
		_model = null;
		_modelPointer = null;
		_xPathSubscription = null;
		String path2 = path;
		while (path2.endsWith("]"))
		{
			int i = path2.lastIndexOf('[');
			if ( i >=  0)
			{
				String parentPath = path2.substring(0, i); 
				try {
					Pointer p = getDataSource().getJXPathContext().getPointer(parentPath);
					Object obj = p.getValue();
					if ( obj instanceof DataModelCollection)
					{
						_model = (DataModelCollection) obj;
						_modelPointer = p;
						_xPathSubscription = p.asPath();
						break;
					}
				} catch (JXPathException e) {
				}
				path2 = parentPath;
			}
		}
		parseNoModel(path);
	}

	/**
	 * @param path
	 */
	private void parseNoModel(String path) {
		partialModel = true;
		items = new Vector ();
		// Buscar los hijos
		try { 
		    JXPathContext ctx = getDataSource().getJXPathContext();
			Iterator  it = ctx.iteratePointers(path);
			while (it.hasNext())
			{
				Pointer p = (Pointer) it.next();
				Object obj = p.getValue();
				// Si algún hijo es ilegítimo, denegar la paternidad
				if ( obj instanceof DataModelNode && ( (DataModelNode)obj).getContainer() != _model)
				{
					_model = null;
					_modelPointer = null;
				}
				items.add(ctx.getPointer(p.asPath()));
			}
		} catch (JXPathException e) {
			items = null;
		}
	}


	public DataModelCollection getUpdateableListModel ()
	{
		smartParse();
		return _model;
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.binder.BinderImplementation#getXPathSubscription()
	 */
	protected String getXPathSubscription() {
		return _xPathSubscription;
	}

	public String getModelXPath ()
	{
		if ( _model == null)
			return null;
		else
			return _xPathSubscription;
	}
	
	/**
	 * @return Returns the items.
	 */
	public List getPointers() {
		smartParse();
		return items;
	}
	
	public ModelProxy createModel ()
	{
		smartParse();
		
		if (getDataSource() == null)
			return null;
		if (_model == null && items == null)
			return null;
		else if (_model == null)
			return new ListModelProxy (this);
		else if ( partialModel )
			return new PartialModelProxy (this);
		else
			return new FullModelProxy (this);
		
	}


	public JXPathContext getJXPathContext() {
		smartParse();

		if (_jxpContext == null && getDataSource() != null && _model != null && _modelPointer != null)
		{
			_jxpContext = getDataSource().getJXPathContext().getRelativeContext(_modelPointer); 
		}
		return _jxpContext;
	}

	public boolean isValid() {
		return true;
	}

	public void onUpdate (XPathEvent event) {
		super.onUpdate(event);

		if ( event instanceof XPathRerunEvent)
			_jxpContext = null;
	}

	
}
