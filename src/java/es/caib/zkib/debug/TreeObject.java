package es.caib.zkib.debug;

import java.util.Vector;

public abstract class TreeObject {
	TreeObject parent;
	Vector children;
	
	public TreeObject getParent() {
		return parent;
	}

	public void setParent(TreeObject parent) {
		this.parent = parent;
	}
	
	public boolean isLeaf ()
	{
		return getChildren().isEmpty();
	}
	
	public Vector getPath (TreeObject grandParent)
	{
		if (this == grandParent || parent == null)
			return new Vector();
		Vector tillThis = parent.getPath(grandParent);
		tillThis.add(new Integer(parent.getChildren().indexOf(this)));
		return tillThis;
	}
	
	public abstract String getKey ();
	public abstract String getValue ();
	public abstract Vector createChildren ();
	
	public Vector getChildren ()
	{
		if (children == null)
			children = createChildren ();
		return children;
	}
}
