package es.caib.zkib.binder.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.event.TreeDataEvent;

import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;


@SuppressWarnings("unchecked")
public class TreeModelProxyNode implements XPathSubscriber {

	Pointer pointer;
	Object hint;
	TreeModelProxyNode children [];
	TreeModelProxyNode parent;
	FullTreeModelProxy modelProxy;
	private String fullXPath = null;
	boolean detached = false;
	
	public TreeModelProxyNode(FullTreeModelProxy proxy) {
		super();
		pointer = proxy.getBinder().getPointer();
		parent = null;
		this.modelProxy = proxy;
		subscribe ();
	}

	public TreeModelProxyNode(TreeModelProxyNode parent, Pointer pointer, Object hint) {
		super();
		this.parent = parent;
		this.pointer = (Pointer) pointer.clone();
		this.hint = hint;
		this.modelProxy = parent.modelProxy;
		subscribe ();
	}

	private void subscribe() {
		modelProxy.getBinder().getDataSource().subscribeToExpression(getXPath(),this);

		ChildXPathQuery querys[] = modelProxy.getChildXPathQuerys();
		
		for (int i = 0; i < querys.length; i++)
		{
			modelProxy.getBinder().getDataSource().subscribeToExpression(
					XPathUtils.concat(getXPathPrefix(),querys[i].getXPath()),this);
		}
	}


	public void detach() {
		unsubscribe();
		parent = null;
		for (int j = 0 ; children != null && j < children.length; j++)
		{
			children[j].detach();
		}
		children = null;
		detached = true;
	}

	/**
	 * 
	 */
	private void unsubscribe() {
		modelProxy.getBinder().getDataSource().unsubscribeToExpression(getXPath(),this);

		ChildXPathQuery querys[] = modelProxy.getChildXPathQuerys();
		for (int i = 0; i < querys.length; i++)
		{
			modelProxy.getBinder().getDataSource().unsubscribeToExpression(XPathUtils.concat(getXPathPrefix(),querys[i].getXPath()),this);
		}
	}

	/**
	 * @return Returns the parent.
	 */
	public TreeModelProxyNode getParent() {
		return parent;
	}

	/**
	 * @return Returns the xPath.
	 */
	public Pointer getPointer() {
		return pointer;
	}
	
	public Object getValue()
	{
		if (pointer == null)
			return null;
		else
			return pointer.getValue();
	}
		
	public Object getHint () {
		return hint;
	}

	public void onUpdate(XPathEvent event) {
		if (!detached)
		{
			if (event instanceof XPathCollectionEvent)
				onListChange ( (XPathCollectionEvent) event);
			else if (event instanceof XPathRerunEvent )
			{
				if (getXPath().equals(event.getXPath()))
					refresh();
			}
		}
	}
	
	/**
	 * @return Returns the children.
	 */
	public TreeModelProxyNode[] getChildren() {
		if (children == null && !detached)
		{
			searchChildren();
		}
		return children;
	}
	/**
	 * @param node
	 * @return
	 */
	protected TreeModelProxyNode[] searchChildren() {
		Vector work = new Vector ();
		JXPathContext ctx = modelProxy.getBinder().getDataSource().getJXPathContext();
		ctx = ctx.getRelativeContext(pointer);
		ChildXPathQuery queries [] = modelProxy.getChildXPathQuerys();
		for (int i = 0 ; i < queries.length; i ++)
		{
			ChildXPathQuery query = queries[i];
			Iterator pointers = ctx.iteratePointers(query.getXPath());
			while (pointers.hasNext())
			{
				Pointer pointer = (Pointer) pointers.next();
				if (pointer.getNode() != null)
				{
					TreeModelProxyNode child = new TreeModelProxyNode(this, pointer, query.getHint());
					work.add(child);
				}
			}
			
		}
		children = (TreeModelProxyNode[]) work.toArray(new TreeModelProxyNode[work.size()]);
		return children;
	}

	
	/**
	 * @param node
	 * @return
	 */
	private void addChild(String path) {
		if (detached)
			throw new UiException("Cannot add child to a detached TreeModelProxyNode");
		JXPathContext ctx = modelProxy.getBinder().getDataSource().getJXPathContext();
		ctx = ctx.getRelativeContext(pointer);
		ChildXPathQuery queries [] = modelProxy.getChildXPathQuerys();

		String thisPath = XPathUtils.concat (modelProxy.getBinder().getDataSource().getRootPath(),getXPathPrefix());
		
		for (int i = 0 ; i < queries.length; i ++)
		{
			ChildXPathQuery query = queries[i];
			Iterator pointers = ctx.iteratePointers(query.getXPath());
			while (pointers.hasNext())
			{
				Pointer pointer = (Pointer) pointers.next();
				String newPath  = XPathUtils.concat(thisPath,pointer.asPath());
				if (path.equals(newPath))
				{
					int size = children!=null ? children.length : 0;
					TreeModelProxyNode[] work = new TreeModelProxyNode[size+1];
					for (int j = 0 ; j < size; j++)
					{
						work[j] = children[j];
					}
					TreeModelProxyNode child = new TreeModelProxyNode(this, pointer, query.getHint());
					work [size] = child;
					children = work;
					modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_ADDED, this, size, size));
				}
			}
		}
	}


	public void onListChange(XPathCollectionEvent event) {
		if (!detached)
		{
			String childPath = event.getXPath()+"["+(event.getIndex()+1)+"]";
			if (event.getType() == XPathCollectionEvent.ADDED)
			{
				addChild (childPath);
			}
			if (event.getType() == XPathCollectionEvent.DELETED)
			{
				deleteChild(childPath);
			}
			if (event.getType() == XPathCollectionEvent.RECREATED)
			{
				refresh();
			}
		}
	}

	/**
	 * 
	 */
	void refresh() {
		if (!detached)
		{
			unsubscribe();
			if (children != null)
			{
				if (children.length > 0)
				{
					modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_REMOVED, this, 0, children.length -1));
					for (int i = 0; i < children.length; i++)
					{
						children[i].detach();
					}
				}
				
		
				searchChildren();
				
				if (children.length > 0)
					modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_ADDED, this, 0, children.length -1));
			}
			subscribe ();
		}
	}

	/**
	 * @param childPath
	 */
	private void deleteChild(String path) {
		ChildXPathQuery queries [] = modelProxy.getChildXPathQuerys();

		String thisPath = XPathUtils.concat(modelProxy.getBinder().getDataSource().getRootPath(),getXPathPrefix());
		
		// Cerquem si tenim fills (n'hi ha vegades que children arriba null):
		if (children==null) 
			children = searchChildren();
		
		for (int i = 0 ; i < children.length; i ++)  
		{
			String newPath  = XPathUtils.concat(thisPath,children[i].pointer.asPath());
			if (path.equals(newPath))
			{
				int size = children.length;
				if (size > i)
				{
					children[i].detach();
					TreeModelProxyNode[] work = new TreeModelProxyNode[size-1];
					for (int j = 0 ; j < i; j++)
					{
						work[j] = children[j];
					}
					for (int j = i+1; j < size; j++)
					{
						work[j-1] = children[j];
					}
					children = work;
					modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_REMOVED, this, i, i));
				}
			}
		}
	}

	/**
	 * @return
	 */
	private String getXPathPrefix() {
		String thisPath = getXPath();
		if ( thisPath.equals("/"))
			thisPath = "";
		return thisPath;
	}
	
	protected String getXPath ()
	{
		if (parent == null)
		{
			return modelProxy.getBinder().getXPath();
		}
		else if (fullXPath != null)
		{
			return fullXPath;
		}
		else
		{
			String parentPath = parent.getXPath();
			if (parentPath.equals ("/"))
				fullXPath = pointer.asPath();
			else
				fullXPath = XPathUtils.concat(parent.getXPath(),pointer.asPath());
			return fullXPath;
		}
	}

	public void sortChildren (Comparator<TreeModelProxyNode> comparator)
	{
		TreeModelProxyNode[] original = Arrays.copyOf(children, children.length);
		Arrays.sort(children, comparator);

//		refreshTree();
	}
	
	public void refreshTree ()
	{
		if (!detached && children.length > 0)
		{
			modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.CONTENTS_CHANGED, this, 0, children.length -1));
		}
	}
	
}
