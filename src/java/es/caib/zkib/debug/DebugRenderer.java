package es.caib.zkib.debug;

import java.util.HashMap;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

public class DebugRenderer implements TreeitemRenderer {

	public void render(Treeitem item, Object arg1) throws Exception {
		Treecell codi = new Treecell ();
		Treerow row = new Treerow();
		if (arg1 instanceof TreeObject)
		{
			TreeObject o = (TreeObject) arg1;
			codi.setLabel(o.getKey()+" "+
				o.getValue());
		}
		codi.setParent(row);
		row.setParent(item);
	}

}
