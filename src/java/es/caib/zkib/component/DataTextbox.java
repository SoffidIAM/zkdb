package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.xml.HTMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.render.SmartWriter;

import es.caib.zkib.binder.BindContext;
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
	private String placeholder;
	
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
			DataSource ds = getTopDatasource();
			if (ds instanceof Component)
				smartUpdate("dsid", ((Component) ds).getUuid());
			else
				smartUpdate("dsid", null);
		}
		else
			smartUpdate("dsid", null);
	}

	public DataSource getTopDatasource() {
		DataSource ds = binder.getDataSource();
		if (ds != null )
			do {
				if (ds instanceof BindContext)
					ds = ((BindContext)ds).getDataSource();
				else
					break;
			} while (true);
		return ds;
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

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getInnerAttrs() {
		final StringBuffer sb =
			new StringBuffer(64).append(super.getInnerAttrs());
		HTMLs.appendAttribute(sb, "placeholder", placeholder);
		if (binder.getDataPath() != null)
		{
			DataSource ds = getTopDatasource();
			if (ds != null && ds instanceof Component)
				HTMLs.appendAttribute(sb, "dsid", ((Component)ds).getUuid());
				
		}
		return sb.toString();
	}

	@Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final String uuid = getUuid();		

		if (isMultiline())
		{
			wh.write("<textarea id=\"").write(uuid).write("\" z.type=\"zul.datasource.DataTxbox\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write(">")
			.write(getAreaText())
			.write("</textarea>");
		}
		else
		{
			wh.write("<input id=\"").write(uuid).write("\" z.type=\"zul.datasource.DataTxbox\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("/>");
		}
	}


}
