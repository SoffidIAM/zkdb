package es.caib.zkib.component;

import java.util.Collection;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;
import java.util.Iterator;// TODO: Test is needed


public class DataLabel extends org.zkoss.zul.Label implements XPathSubscriber {
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
	public String getValue() {
		String oldValue = super.getValue();
		Object newValue = binder.getValue(oldValue);
		String v = "";
		if (newValue != null) {
			if (newValue instanceof Collection) {
				Collection newValueCollection = (Collection) newValue;
				String finalValue = "";
				for (Iterator iterator = newValueCollection.iterator(); iterator
						.hasNext();) {
					String currentNewValue = (String) iterator.next();
					finalValue += currentNewValue
							+ (iterator.hasNext() ? ", " : "");
				}
			} else {
				v = newValue.toString();
			}
		}
		if (!oldValue.equals(v)) {
			setValue(v);
		}
		return v;
	}

	public void onUpdate(XPathEvent event) {
		if (binder.getValue() == null)
			setValue("");
		else
			setValue(binder.getValue().toString());
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
		if (bind != null) {
			if (binder.getValue() == null)
				setValue("");
			else
				setValue(binder.getValue().toString());
		}
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
		DataLabel clone = (DataLabel) super.clone();
		clone.binder = new SingletonBinder(clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}

}
