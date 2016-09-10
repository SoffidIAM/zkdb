package es.caib.zkib.binder.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zul.event.TreeDataEvent;
import org.zkoss.zul.event.TreeDataListener;

import es.caib.zkib.binder.SingletonBinder;

@SuppressWarnings("unchecked")
public class FullTreeModelProxy implements TreeModelProxy {
	TreeModelProxyNode root;
	Vector v = null;
	Vector listeners;
	Vector childrenXPath;
	SingletonBinder binder;
	
	public FullTreeModelProxy(SingletonBinder binder) {
		this.binder = binder;
		childrenXPath = new Vector ();
		root = new TreeModelProxyNode (this);

		listeners = new Vector ();
		v = new Vector ();

	}

	
	public void addChildXPathQuery (String xPath, Object hint)
	{
		childrenXPath.add(new ChildXPathQuery (xPath, hint));
		root.refresh ();
	}

	public ChildXPathQuery[] getChildXPathQuerys ()
	{
		return (ChildXPathQuery[]) childrenXPath.toArray(new ChildXPathQuery[0]);
	}
	
	protected void sendEvent(TreeDataEvent event) {
		Iterator it = listeners.iterator();
		while (it.hasNext())
		{
			TreeDataListener listener = (TreeDataListener) it.next ();
			listener.onChange(event);
		}
	}

	public void unsubscribe() {
		root.detach ();
	}

	
	
	public boolean isLeaf(Object arg0) {
		if (arg0 == null)
			return true;
		TreeModelProxyNode node = (TreeModelProxyNode) arg0;
		return  !node.isContainer();
	}

	public Object getChild(Object arg0, int arg1) {
		TreeModelProxyNode node = (TreeModelProxyNode) arg0;
		if (arg1 >= node.getChildren().length || arg1 < 0)
			return null;
		else
			return  node.getChildren()[arg1];
	}

	public int getChildCount(Object arg0) {
		TreeModelProxyNode node = (TreeModelProxyNode) arg0;
		return  node.getChildren().length;
	}

	public Object getRoot() {
		return root;
	}

	public void addTreeDataListener(TreeDataListener arg0) {
		listeners.add(arg0);
		
	}

	public void removeTreeDataListener(TreeDataListener arg0) {
		listeners.remove(arg0);
	}

	/**
	 * Returns an integer array to represent the path from parent(exclusive) to lastNode(inclusive).
	 * <br>notice:<br>
	 * The path has to be in "parent" to "lastNode" order<br>
	 * Ex: {1,0,2}<br>
	 * 	1. Go to the parent's child at index(1);<br>
	 *  2. Go to the index(1)'s child at index(0);<br>
	 *  3. Go to the index(0)'s child at idnex(2) -- the lastNode;<br>
	 * If parent is the same as lastNode, return null or empty array. 
	 * 
	 * @param parent the origin of Path
	 * @param lastNode the destination of Path
	 * @return an integer array to represent the path from parent to lastNode.
	 */
	public int[] getPath(Object parent, Object lastNode){
		Vector work = new Vector ();
		TreeModelProxyNode current = (TreeModelProxyNode) lastNode;
		while (current != parent && current != null && current.getParent() != null)
		{
			TreeModelProxyNode brothers [] = current.getParent().getChildren();
			int i;
			for (i = 0; i < brothers.length && brothers[i] != current ; i++)
			{
				// Nothing to do
			}
			work.add(new Integer(i));
			current = current.getParent();
		}
		int max = work.size ();
		int result [] = new int [max];
		for (int i = 0; i < max; i ++)
		{
			Integer integer = (Integer) work.get(max - i - 1);
			result [i] = integer.intValue();
		}
		return result;
	}


	public String getBind(Object parent, int index) {
		TreeModelProxyNode node = (TreeModelProxyNode) parent;
		TreeModelProxyNode child  =  node.getChildren()[index];
		return binder.getDataSource()+":"+child.getPointer().asPath();
	}


	/**
	 * @return Returns the binder.
	 */
	public SingletonBinder getBinder() {
		return binder;
	}


	public String getXPath(int[] treeitem) {
		TreeModelProxyNode node = getTreeModelProxyNode(treeitem);
		if (node == null)
			return null;
		return node.getXPath();
	}

	public TreeModelProxyNode getTreeModelProxyNode(int[] treeitem) {
		TreeModelProxyNode node = root;
		for (int i = 0; i < treeitem.length; i++)
		{
			TreeModelProxyNode[] nodes = node.getChildren();
			if (nodes.length <= treeitem[i])
				return null;
			node = nodes [ treeitem [i] ];
		}
		if (node == null)
			return null;
		return node;
	}
	
}

