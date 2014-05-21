package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Listitem;

import es.caib.zkib.binder.SingletonBinder;

public class MasterComboItem extends Comboitem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7982352127626817466L;
	private String bind;
	private String labelbind;
	SingletonBinder binder = new SingletonBinder(this);
	SingletonBinder labelbinder = new SingletonBinder(this);
	
	
	public MasterComboItem() {
		super();
	}


	/**
	 * @return Returns the bind.
	 */
	public String getBind() {
		return bind;
	}


	/**
	 * @param bind The bind to set.
	 */
	public void setBind(String bind) {
		this.bind = bind;
	}

	/**
	 * @return Returns the bind.
	 */
	public String getLabelBind() {
		return labelbind;
	}


	/**
	 * @param bind The bind to set.
	 */
	public void setLabelBind(String bind) {
		this.labelbind = bind;
	}


	public String getLabel() {
		String label = (String) labelbinder.getValue();
		if ( label == null )
			label = super.getLabel();
		if (label == null)
			label = "";
		return label;
	}

	public Object getValue() {
		Object value = binder.getValue();
		if ( value == null )
			value = super.getValue();
		if (value == null)
			value = "";
		return value;
	}


	
}
