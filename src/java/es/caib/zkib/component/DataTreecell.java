package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Treecell;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataTreecell extends Treecell implements XPathSubscriber {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4115350210074177684L;
	SingletonBinder binder = new SingletonBinder(this);
	
	public DataTreecell() {
		super();
	}

	public DataTreecell(String arg0) {
		super(arg0);
	}

	public DataTreecell(String arg0, String arg1) {
		super(arg0, arg1);
	}

	public String getLabel() {
		String label = (String) binder.getValue();
		if ( label == null )
			label = super.getLabel();
		if (label == null)
			label = "";
		return label;
	}

	public String getBind() {
		return binder.getDataPath();
	}

	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	public void onUpdate (XPathEvent event) {
		if (getPage() != null)
			invalidate();
	}

	public void setBind(String bind) {
		binder.setDataPath(bind);
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
		DataTreecell clone = (DataTreecell) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
}
