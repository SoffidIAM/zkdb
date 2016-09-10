package es.caib.zkib.binder.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.event.TreeDataEvent;

import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathNotFoundException;
import es.caib.zkib.jxpath.Pointer;


@SuppressWarnings("unchecked")
public class TreeModelProxyNode implements XPathSubscriber {

	String pointerPath;
	Object hint;
	TreeModelProxyNode children [];
	TreeModelProxyNode parent;
	FullTreeModelProxy modelProxy;
	private String fullXPath = null;
	boolean detached = false;
	private String localPath;
	
	public TreeModelProxyNode(FullTreeModelProxy proxy) {
		super();
//		pointerPath = proxy.getBinder().getPointer().asPath();
		pointerPath = ".";
		localPath = pointerPath;
		parent = null;
		this.modelProxy = proxy;
		subscribe ();
	}

	public TreeModelProxyNode(TreeModelProxyNode parent, String base, String localPath, Object hint) {
		super();
		this.parent = parent;
		this.pointerPath = XPathUtils.concat(base, localPath);
		this.localPath = localPath;
		this.hint = hint;
		this.modelProxy = parent.modelProxy;
		subscribe ();
	}

	private void subscribe() {
		modelProxy.getBinder().getDataSource().subscribeToExpression(getXPath(),this);

		ChildXPathQuery queries[] = modelProxy.getChildXPathQuerys();
		
		for (int i = 0; i < queries.length; i++)
		{
			modelProxy.getBinder().getDataSource().subscribeToExpression(
					XPathUtils.concat(getXPathPrefix(),queries[i].getXPath()),this);
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
	public Pointer getPointer() throws JXPathNotFoundException {
		JXPathContext ctx = modelProxy.getBinder().getJXPathContext();
		return ctx.getPointer(pointerPath);
	}
	
	public Object getValue()
	{
		try {
			return getPointer().getValue();
		} catch (JXPathNotFoundException e) {
			return null;
		}
	}
		
	public Object getHint () {
		return hint;
	}

	private boolean duringOnRerunXPath = false;
	
	private boolean isDuringOnRerunXPath ()
	{
		return duringOnRerunXPath || (parent != null && parent.isDuringOnRerunXPath());
	}
	
	public void onUpdate(XPathEvent event) {
		if (!detached)
		{
			if (event instanceof XPathCollectionEvent)
				onListChange ( (XPathCollectionEvent) event);
			else if (event instanceof XPathRerunEvent  && !isDuringOnRerunXPath())
			{
				duringOnRerunXPath = true;
				try
				{
					String baseEventPath = ((XPathRerunEvent) event).getBaseXPath();
					if (parent == null  || parent.children == null)
					{
						refresh ();
					}
					else if (getXPath().equals(baseEventPath))
					{
						for (int i = 0; i < parent.children.length; i++)
						{
							if (parent.children[i] == this)
							{
								parent.modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_ADDED, parent, i, i));
								if (children != null)
								{
									for (int j = 0; j < children.length; j++)
									{
										children[j].detach();
									}
								}
									
								searchChildren();
								parent.modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_REMOVED, parent, i, i));
							}
						}
					}
					else 
					{
						ChildXPathQuery queries[] = modelProxy.getChildXPathQuerys();
						
						for (int i = 0; i < queries.length; i++)
						{
							String path = XPathUtils.concat(getXPathPrefix(),queries[i].getXPath());
							if (path.equals(baseEventPath))
							{
								refresh();
							}
						}
						
					}
				} finally {
					duringOnRerunXPath =false;
				}
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
	public boolean isContainer() {
		TreeModelProxyNode[] ch = getChildren();
		if (ch != null && ch.length > 0)
			return true;
		
		try
		{
			JXPathContext ctx = modelProxy.getBinder().getDataSource().getJXPathContext();
			ctx = ctx.getRelativeContext(getPointer());
			ChildXPathQuery queries [] = modelProxy.getChildXPathQuerys();
			for (int i = 0 ; i < queries.length; i ++)
			{
				ChildXPathQuery query = queries[i];
				try
				{
					Object obj = ctx.getValue(query.getXPath());
					if (obj != null)
					{
						if (obj instanceof Collection ||
								obj instanceof DataModelCollection )
							return true;
					}
				
				} catch (Exception e) {
				}
			}
		} catch (JXPathNotFoundException e) {
		}
		return false;
	}

	/**
	 * @param node
	 * @return
	 */
	protected TreeModelProxyNode[] searchChildren() {
		Vector work = new Vector ();
		try
		{
			JXPathContext ctx = modelProxy.getBinder().getDataSource().getJXPathContext();
			ctx = ctx.getRelativeContext(getPointer());
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
						TreeModelProxyNode child = new TreeModelProxyNode(this, 
								pointerPath,
								pointer.asPath(),
								query.getHint());
						work.add(child);
					}
				}
				
			}
		} catch (JXPathNotFoundException e) {
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
		JXPathContext ctx;
		try 
		{
			ctx = modelProxy.getBinder().getDataSource().getJXPathContext();
			ctx = ctx.getRelativeContext(getPointer());
		} catch (JXPathNotFoundException e) {
			// Ignore this event
			return ;
		}
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
					TreeModelProxyNode child = new TreeModelProxyNode(this, 
							pointerPath, 
							pointer.asPath(),
							query.getHint());
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
			// Refresh pointer
			try {
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
					
					if (children != null && children.length > 0)
						modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_ADDED, this, 0, children.length -1));
				}
				subscribe ();
			} catch (JXPathNotFoundException e) {
				if (children != null && children.length > 0)
				{
					modelProxy.sendEvent ( new TreeDataEvent (modelProxy, TreeDataEvent.INTERVAL_REMOVED, this, 0, children.length -1));
					for (int i = 0; i < children.length; i++)
					{
						children[i].detach();
					}
				}
			}
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
			String newPath  = XPathUtils.concat(thisPath,children[i].localPath);
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
				fullXPath = pointerPath;
			else
				fullXPath = XPathUtils.concat(parent.getXPath(),localPath);
			if (! fullXPath.startsWith("/"))
				fullXPath = "/"+fullXPath;
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

	public String getLocalPath() {
		return localPath;
	}
	
}
