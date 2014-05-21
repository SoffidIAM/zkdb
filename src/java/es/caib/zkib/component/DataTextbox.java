package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataTextbox extends org.zkoss.zul.Textbox implements XPathSubscriber {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6447081728620764693L;
	private SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate = false;
	
	
	public void undo(){
		binder.setOldValue();
	}
	
	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#getBind()
	 */
	public String getBind() {		
		return binder.getDataPath();
		
	}

	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#setBind(java.lang.String)
	 */

	public void setText(String value) throws WrongValueException {
		super.setText(value);
		if (! duringOnUpdate )
		{
			binder.setValue(value);
		}
	}

	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#setBind(java.lang.String)9
	 */

	public void setValue(String value) throws WrongValueException {
		super.setValue(value);
		if (! duringOnUpdate )
		{
			binder.setValue(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.impl.InputElement#validate(java.lang.Object)
	 */
	@Override
	protected void validate(Object value) throws WrongValueException {
		if (! duringOnUpdate)
			super.validate(value);
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#getDataSource()
	 */
	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#setBind(java.lang.String)
	 */
	public void setBind(String bind) {
		binder.setDataPath(bind);
		if (bind != null)
		{
			refreshValue ();
		}
	}

	public void onUpdate (XPathEvent event) {
		refreshValue ();
	}

	private void refreshValue ()
	{
		Object newVal = binder.getValue();
		if (newVal == null)
			newVal = "";
		try {
			duringOnUpdate = true;
			setValue( newVal.toString() );
		} catch (WrongValueException e) {
			// Ignore
		} finally {
			duringOnUpdate = false;
		}
		
		if (!binder.isVoid () && ! binder.isValid())
			super.setDisabled (true);
		else
			super.setDisabled(effectiveDisabled);
	}
	
	public boolean effectiveDisabled = false;
	/* (non-Javadoc)
	 * @see org.zkoss.zul.impl.InputElement#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		effectiveDisabled = disabled;
		super.setDisabled(disabled);
	}

	public void setPage(Page page) {
		super.setPage(page);
		binder.setPage(page);
	}
	
	public void setOldValue() {
		binder.setOldValue();
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binder.setParent(parent);
	}

	public Object clone() {
		DataTextbox clone = (DataTextbox) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		clone.effectiveDisabled = effectiveDisabled;
		return clone;
	}

}
