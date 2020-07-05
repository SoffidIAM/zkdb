package es.caib.zkib.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.tree.DataTreeitemRenderer;
import es.caib.zkib.binder.tree.FullTreeModelProxy;
import es.caib.zkib.binder.tree.TreeModelProxy;
import es.caib.zkib.binder.tree.TreeModelProxyNode;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.xml.XmlDataNode;
import es.caib.zkib.datasource.ChildDataSourceImpl;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.SerializableEventListener;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Variables;

public class DataTree extends Tree implements XPathSubscriber, BindContext, DataSource, AfterCompose {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7972552691186027886L;
	SingletonBinder treeBinder = new SingletonBinder (this);
	SingletonBinder valueBinder = new SingletonBinder(this);
	ChildDataSourceImpl dsImpl = null;
	boolean autocommit = true;
	boolean composed = false;
	Vector<MasterTreeitem> masterTreeItems = new Vector<MasterTreeitem>();
	HashMap imagesBinding;
	private String selectedItemXPath;
	// Per a les icones del binding de l'arbre
	public void setImageBinding(String[][] imgBinding) {
		if (imgBinding !=null) {
			imagesBinding = new HashMap();
			for (int i=0; i < imgBinding.length; i++) {
				if (imgBinding[i]!=null && imgBinding[i].length ==2) {
					imagesBinding.put(imgBinding[i][0], imgBinding[i][1]);
				}
			}
		}
	}
	
	public String getImageBinding(String key) {
		return imagesBinding != null ? (String) imagesBinding.get(key) : null;
	}
	
	public DataTree() {
		super();
	}

	public String getBind() {
		return valueBinder.getDataPath();
	}

	public void setBind(String bind) {
		valueBinder.setDataPath(bind);
	}

	public String getDataPath() {
		return treeBinder.getDataPath();
	}
	
	public String getSelectedItemXPath() 
	{
		return selectedItemXPath;
	}

	public void setDataPath(String bind) throws Exception {
		
		
		treeBinder.setDataPath (bind);

		if (composed)
			applyModel();
	}

	private void applyModel() throws Exception {
		if (composed && treeBinder.getDataPath() != null)
		{
			FullTreeModelProxy lm = new FullTreeModelProxy(treeBinder); 

			for (Iterator<MasterTreeitem> it=masterTreeItems.iterator(); it.hasNext();)
			{
				MasterTreeitem item = it.next();
				lm.addChildXPathQuery(
						item.getPath(),
						item
						);
			}
			TreeModel oldListModel = getModel ();
			if ( oldListModel != null && oldListModel instanceof TreeModelProxy)
			{
				((TreeModelProxy) oldListModel).unsubscribe ();
			}

			setModel ( lm );
			this.setTreeitemRenderer(new DataTreeitemRenderer(this));
	
			if (dsImpl == null)
				dsImpl = new ChildDataSourceImpl ();
			
			dsImpl.setDataSource(treeBinder.getDataSource());
			dsImpl.setRootXPath(treeBinder.getXPath());
	
			Treeitem item = getSelectedItem();
			int path[] = getTreeItemPath(item);
			if (path.length > 0)
			{
				selectedItemXPath = lm.getXPath(path);
				dsImpl.setXPath(selectedItemXPath);
				
			}
		}
	}

	public DataSource getDataSource() {
		return treeBinder.getDataSource();
	}

	public void sendEvent(XPathEvent event) {
		dsImpl.sendEvent(event);
	}

	/** Esto recibe los eventos asociados al valor resultado, no al modelo */
	public void onUpdate (XPathEvent event) {
		if (event instanceof XPathValueEvent)
		{
			updateValue();
		}
		if (event instanceof XPathRerunEvent)
		{
			try {
				applyModel();
			} catch (Exception e) {
				throw new UiException(e);
			}
		}
		if (event instanceof XPathCollectionEvent)
		{
			/*
			XPathCollectionEvent e = (XPathCollectionEvent) event;
			if ( e.getType() == XPathCollectionEvent.FOCUSNODE && getModel() instanceof TreeModelProxy)
			{
				TreeModelProxy mp = (TreeModelProxy) getModel();
				int i = mp.getPosition(e.getIndex());
				if ( i >= 0 )
				{
					setSelectedIndex(i);
				}
			}
			*/
		}
		
	}

	/**
	 * 
	 */
	private void updateValue() {
		Object value = valueBinder.getValue();
		if (value != null)
		{
			Iterator it = getItems().iterator();
			while (it.hasNext())
			{
				Treeitem item = (Treeitem) it.next();
				if ( item.getValue() == null )
				{
					renderItem(item);
				}
				if (value.equals(item.getValue()))
				{
					setSelectedItem(item);
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
		if (onSelectListener == null)
		{
			onSelectListener = new DummyEventListener ();
			this.addEventListener("onSelect", onSelectListener);
			
		}
	}
	
	private final class OnOpenTreeEventListener extends SerializableEventListener {
		private static final long serialVersionUID = 1L;

		public void onEvent(Event arg0) throws Exception {
			Treeitem item = (Treeitem) arg0.getData();
			item.setOpen(true);
		}
	}

	class DummyEventListener implements EventListener, Serializable
	{
		private static final long serialVersionUID = 1L;

		public void onEvent(org.zkoss.zk.ui.event.Event arg0) {// NOTHING TO DO
		};
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.Listbox#smartUpdate(java.lang.String, java.lang.String)
	 */
	public void smartUpdate(String attr, String value) {
		if ("selectedItem".equals(attr) || "select".equals(attr))
		{
			Treeitem item = getSelectedItem();

			if (item == null)
			{
				if (getModel () != null && getModel () instanceof TreeModelProxy && dsImpl != null)
				{
					dsImpl.setXPath(null);
					selectedItemXPath = null;
				}
				valueBinder.setValue(null);
			}
			else
			{
				if (getModel () != null && getModel () instanceof TreeModelProxy && dsImpl != null)
				{
					int path [] = getTreeItemPath (item);
					selectedItemXPath = ( (TreeModelProxy) getModel()).getXPath(path);
					dsImpl.setXPath(selectedItemXPath);
				}
				valueBinder.setValue(item.getValue());
			}
 		}
		super.smartUpdate(attr, value);
	}

	/**
	 * @param newChild
	 */
	protected void addMasterTreeItem(Component newChild) {
		masterTreeItems.add((MasterTreeitem) newChild);
		if (composed)
		{
			try {
				applyModel();
			} catch (Exception e) {
				throw new UiException(e);
			}
		}
	}

	public String getXPath() {
		return treeBinder.getXPath();
	}

	public void unsubscribeToExpression(String xpath, XPathSubscriber subscriber) {
		dsImpl.unsubscribeToExpression(xpath, subscriber);
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
			if (treeBinder != null && treeBinder.getDataSource() != null)
				treeBinder.getDataSource().commit();
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

	
	public Treeitem addNew (String path)
	{
		if (autocommit) commit ();
		
		try {
			Treeitem item = getSelectedItem();
			SingletonBinder data;
			if (item == null) //No s'ha seleccionat cap node o no té fills
			{
				data = treeBinder;
			}
			else
			{
				data = (SingletonBinder) item.getAttribute(BindContext.BINDCTX_ATTRIBUTE);
				if (item.getTreechildren() == null)
					item.appendChild(new Treechildren());
			}
			
			if (data == null)
				throw new UiException ("Not allowed to create records without a model");

			DataModelCollection collection = (DataModelCollection) data.getJXPathContext().getValue(path);
			DataModelNode nouNode = collection.newInstance(); // Propaguem event onchange
			
			// Seleccionem el nou element a l'arbre (u88683):
			String nouXPath = nouNode.getXPath();
			// Comprovem que item != null (nodes sense pare)
			if (item!=null && !item.isOpen()) // És important que el node estiga EXPANDIT (obert) per trobar el fill nou 
				item.setOpen(true);
			Treeitem node;
			if ( (node = getNodeByXpath(nouXPath))!=null) { // Seleccionem el nou node
				selectItem(node);
				item = node;
			}
			return item; //Retornem el node (informació de la creació)

			
		} catch (ClassCastException e) {
			throw new UiException ("Path "+path+" does not referes a data collection");
		} catch (Exception e) {
			throw new UiException (e);
		}
	}
	
	/**
	 * Cerquem un node de l'arbre per la seua expressió Xpath (by u88683)
	 * @param xpath
	 * @return el node o null si no s'hat trobat
	 */
	private Treeitem getNodeByXpath(String xpath) {
		Collection fillsArbre = getItems();
		for (Iterator it = fillsArbre.iterator(); it.hasNext();) {
			Object n = it.next();
			if (n instanceof Treeitem) {
				Treeitem node = (Treeitem) n;

				Object value = node.getValue();
				if  (value instanceof XmlDataNode) {
					XmlDataNode xn = (XmlDataNode) value;
					if (xpath.equals(xn.getXPath())) {
						return node;
					}
				}
			}
		}		
		return null;
	}
	
	/**
	 * Obre la branca de l'arbre (si no és ja oberta) [by u88683]
	 * @param xpath
	 * @return
	 */
	public boolean obreBrancaByXpath(String xpath) {

		Treeitem node = getNodeByXpath(xpath);
		if (node == null) {
			// Hem de obrir el seus pares
			String[] nivells = xpath.split("/");
			String ruta = "";
			for (int i = 0; i < nivells.length; i++) {
				if (nivells[i].equals(""))
					;
				else {
					ruta += "/"+nivells[i];
					node = getNodeByXpath(ruta);
					if (node != null)
						node.setOpen(true); // Obrim el pare
					else
						return false;
				}

			}
		} else node.setOpen(true);

		setSelectedItem(node);
		
		return true;
	}
	
	/**
	 * Selecciona el node per la ruta que ens han oferit
	 * @param xpath
	 * @return
	 */
	public boolean selectNodeByXpath(String xpath) {//by u88683
		setSelectedItem(null);// Borrem selecció previa
		Treeitem node = getNodeByXpath(xpath);
		if (node == null) {
			// Hem de obrir el seus pares
			String[] nivells = xpath.split("/");
			String ruta = "";
			for (int i = 0; i < nivells.length; i++) {
				if (nivells[i].equals(""))
					;
				else {
					ruta += "/" + nivells[i];
					node = getNodeByXpath(ruta);
					if (node != null) 
						node.setOpen(true);
					else
						return false;
				}

			} 
		} else { // No desplegamos el último nivel
			if (node.getParentItem()!=null) // Por si se ha cerrado manualmente (abrimos hasta el padre)
				node.getParentItem().setOpen(true);
		}
		
		node.setSelected(true);
		return true;
	}

	public void delete ()
	{
		TreeModel lm = getModel ();
		if ( lm == null || ! ( lm instanceof TreeModelProxy))
			throw new UiException ("Not allowed to delete records without a model");
		
		if (autocommit) commit ();
		
		Treeitem item = getSelectedItem();
		if (item != null)
		{
			SingletonBinder data = (SingletonBinder) item.getAttribute(BindContext.BINDCTX_ATTRIBUTE);
			if (data == null)
				throw new UiException("Unable to delete treeitem without context");
			
			Object obj = data.getValue ();
			if (obj == null)
				throw new UiException ("Unable to delete object "+obj);
			if ( obj instanceof DataModelNode)
			{
				( (DataModelNode) obj).delete();
				if (autocommit) commit ();
			}
			else
			{
				XPathUtils.removePath(data.getDataSource(), data.getXPath());
			}
			setSelectedItem(null);
		}
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.datasource.ChildDataSourceImpl#getVariables()
	 */
	public Variables getVariables() {
		return dsImpl.getVariables();
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.Listbox#clearSelection()
	 */
	public void clearSelection() {
		super.clearSelection();
		selectedItemXPath = null;
		if (dsImpl != null) dsImpl.setXPath(null);
	}

	public boolean isCommitPending() {
		if (dsImpl == null) return false;
		else return dsImpl.isCommitPending();
	}


	public void setPage(Page page) {
		super.setPage(page);
		valueBinder.setPage(page);
		treeBinder.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		valueBinder.setParent(parent);
		treeBinder.setParent(parent);
	}

	public Object clone() {
		DataListbox clone = (DataListbox) super.clone();
		clone.valueBinder = new SingletonBinder (clone);
		clone.collectionBinder = new CollectionBinder (clone);
		clone.setDataPath(treeBinder.getDataPath());
		clone.setBind(valueBinder.getDataPath());
		return clone;
	}
	
	protected class ExtraCtrl extends Tree.ExtraCtrl implements Serializable
	{
		//-- Selectable --//
		public void selectItemsByClient(Set selItems) {
			autocommit();
			
			super.selectItemsByClient(selItems);
		}

	}

	private int [] getTreeItemPath (Treeitem item)
	{
		Vector v = new Vector ();
		while (item != null )
		{
			v.add(item.indexOf());
			Treechildren brothers = (Treechildren) item.getParent();
			Component c = brothers.getParent();
			if (c instanceof Treeitem)
				item = (Treeitem) c;
			else
				item = null;
		}
		int work [] = new int [v.size()];
		for (int i = 0; i < v.size(); i ++)
		{
			Integer integer = (Integer) v.get(work.length-1-i);
			work [ i ] = integer.intValue();
		}
		return work;
	}

	public void afterCompose() {
		composed = true;
		addEventListener("onTreeitemOpen", new OnOpenTreeEventListener());
		try {
			applyModel();
		} catch (Exception e) {
			throw new UiException(e);
		}
	}

	public String getRootPath() {
		return dsImpl.getRootPath();
	}
	
	public TreeModelProxyNode getModelProxyNode (Treeitem item)
	{
		int path[] = getTreeItemPath(item);
		if (getModel() instanceof FullTreeModelProxy)
		{
			FullTreeModelProxy ftmp = (FullTreeModelProxy) getModel();
			
			return ftmp.getTreeModelProxyNode(path);
		}
		else
			return null;
	}
	
}
