package es.caib.zkib.debug;

import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;

public class PageTree extends TreeObject {
	Page page;
	
	public PageTree(Page page) {
		super();
		this.page = page;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Vector createChildren() {
		Vector v = new Vector();
		for (Iterator it = page.getRoots().iterator(); it.hasNext();)
		{
			v.add ( new ComponentTree( (Component) it.next()) );
		}
		return v;
	}

	public String getKey() {
		return "Page";
	}

	public String getValue() {
		return page.getId()+" / "+page.getTitle();
	}

}
