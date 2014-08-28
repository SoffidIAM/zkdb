package es.caib.zkib.component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Row;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;

public class DataRow extends Row implements BindContext, XPathSubscriber {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5083390126800434150L;
	SingletonBinder binder = new SingletonBinder(this);
	
	public DataRow() {
		super();
	}

	public String getDataPath() {
		return binder.getDataPath();
	}

	public void setDataPath(String bind) {
		binder.setDataPath(bind);
	}

	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	public void onUpdate(XPathEvent event) {
		if (getPage() != null)
			invalidate();
	}
	
	public JXPathContext getJXPathContext() {
		return binder.getJXPathContext();
	}


	public String getXPath() {
		return binder.getXPath();
	}

	public void setPage(Page page) {
		super.setPage(page);
		binder.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binder.setParent(parent);
	}


	public Object clone() {
		DataRow clone = (DataRow) super.clone();
		for (Component c: (List<Component>) clone.getChildren())
		{
			if (c instanceof IdSpace)
			{
				for (String v: new LinkedList<String>((Set<String>)c.getNamespace().getVariableNames()))
				{
					c.getNamespace().unsetVariable(v, true);
				}
			}
		}
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
	
}
