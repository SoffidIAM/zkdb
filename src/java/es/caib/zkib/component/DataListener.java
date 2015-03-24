package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

import java.util.Iterator;// TODO: Test is needed


public class DataListener extends AbstractComponent implements XPathSubscriber {
	public DataListener() {
		super();
		setVisible(false);
	}

	@Override
	public void redraw(Writer out) throws IOException {
		// Nothing to do
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8760268186730217538L;
	private SingletonBinder binder = new SingletonBinder(this);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.zkoss.zul.Label#getValue()
	 */
	public Object getValue() {
		return binder.getValue();
	}

	public void onUpdate(XPathEvent event) {
		Events.postEvent(new Event("onUpdate", this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#getBind()
	 */
	public String getBind() {
		return binder.getDataPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#setBind(java.lang.String)
	 */
	public void setBind(String bind) {
		binder.setDataPath(bind);
		Events.postEvent(new Event("onUpdate", this));
	}

	public void setPage(Page page) {
		super.setPage(page);
		binder.setPage(page);
		Events.postEvent(new Event("onUpdate", this));
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binder.setParent(parent);
	}

	public Object clone() {
		DataListener clone = (DataListener) super.clone();
		clone.binder = new SingletonBinder(clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}

}
