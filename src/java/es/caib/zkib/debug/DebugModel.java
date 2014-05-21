package es.caib.zkib.debug;

import java.util.Set;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.event.TreeDataListener;


public class DebugModel implements TreeModel {
	Desktop desktop;

	public DebugModel(Desktop desktop) {
		super();
		this.desktop = desktop;
	}

	public DebugModel() {
		super();
		this.desktop = Executions.getCurrent().getDesktop();
	}

	public void addTreeDataListener(TreeDataListener arg0) {
		
	}

	public Object getChild(Object arg0, int arg1) {
		return ((TreeObject )arg0).getChildren().get(arg1);
	}

	public int getChildCount(Object arg0) {
		return ((TreeObject )arg0).getChildren().size();
	}

	Vector getRecursivePath (Object arg0, Object arg1)
	{
		return ((TreeObject) arg1).getPath((TreeObject) arg0);
	}

	public int[] getPath(Object arg0, Object arg1) {
		Vector v = getRecursivePath(arg0, arg1);
		int array[] = new int[v.size()];
		for (int i = 0; i < v.size(); i++)
		{
			array [i] = ((Integer)v.get(i)).intValue();
		}
		return array;
	}

	public Object getRoot() {
		return new DesktopTree(desktop);
	}

	public boolean isLeaf(Object arg0) {
		return ((TreeObject) arg0).isLeaf();
	}

	public void removeTreeDataListener(TreeDataListener arg0) {
	}

}
