package es.caib.zkib.binder.tree;

import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treeitem;

import es.caib.zkib.binder.SingletonBinder;

public interface TreeModelProxy extends TreeModel {
	public String getBind(Object parent, int index);
	
//	public int newInstance () throws Exception; 
	
//	public int getPosition (String xPath);

	public void unsubscribe();

	public void addChildXPathQuery (String xPath, Object hint);
	public ChildXPathQuery[] getChildXPathQuerys ();
	
	public String getXPath (int treeitem[]);
	
}
