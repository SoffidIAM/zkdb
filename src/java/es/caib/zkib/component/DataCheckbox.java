package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Checkbox;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataCheckbox extends Checkbox implements XPathSubscriber {
	/**
	 * 
	 */
	private static final long serialVersionUID = -26350721236864797L;
	SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate = false;

	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.AbstractComponent#setPage(com.potix.zk.ui.Page)
	 */
	public void setPage(Page page) {
		super.setPage(page);
		binder.setPage(page);
	}
	
	public void setParent(Component parent) {
		super.setParent(parent);
		binder.setParent(parent);
	}

	public DataCheckbox() {
		super();
	}

	public void setBind (String bind)
	{
		binder.setDataPath(bind);
		if (bind != null)
		{
			refreshValue ();
		}
	}
	
	public String getBind ()
	{
		return binder.getDataPath();
	}
	
	private void refreshValue ()
	{
		Object newVal = binder.getValue();
		boolean selected;
		
		if (newVal != null)
		{
			if (newVal instanceof Boolean)
				selected = ((Boolean) newVal).booleanValue();
			else if (newVal instanceof String)
				selected = ! newVal.equals("false");
			else if (newVal instanceof Integer)
				selected = ((Integer) newVal).intValue() != 0;
			else if (newVal instanceof Long)
				selected = ((Long) newVal).intValue() != 0;
			else
				selected = true;
		}
		else
			selected = false;
			
		try {
			duringOnUpdate = true;
			setChecked(getLogicalValue(selected));
		} finally {
			duringOnUpdate = false;
		}
		
		if (!binder.isVoid () && ! binder.isValid())
			super.setDisabled (true);
		else
			super.setDisabled(effectiveDisabled);
	}
	
	public void onUpdate (XPathEvent event) {
		refreshValue();
	}

	

	
	
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if ( ! duringOnUpdate)
		{
			binder.setValue(getLogicalValue(checked));
		}
	}
	
	public boolean isChecked() {
		return super.isChecked();
	}

	private boolean effectiveDisabled = false;
	private String logic ="default";

	/* (non-Javadoc)
	 * @see org.zkoss.zul.Checkbox#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		effectiveDisabled = disabled;
		super.setDisabled(disabled);
	}

 	public Object clone() {
		DataCheckbox clone = (DataCheckbox) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
	
	//-- ComponentCtrl --//
	protected Object newExtraCtrl() {
		return new ExtraCtrl();
	}
	
	private Boolean getLogicalValue(boolean checked) {
		// Per defecte retornem el valor del binding..
		if ("inverse".equals(logic))
			return new Boolean(!checked);
		else
			return new Boolean(checked);
	}
	
	/** A utility class to implement {@link #getExtraCtrl}.
	 * It is used only by component developers.
	 */
	protected class ExtraCtrl extends Checkbox.ExtraCtrl {
		//-- Checkable --// 
		public void setCheckedByClient(boolean checked) {
			super.setCheckedByClient(checked);
			if ( ! duringOnUpdate)
			{
				binder.setValue(new Boolean(getLogicalValue(checked)));
			}
		}
	}
	
	/**
	 * sets selection logic
	 * Available values="default | inverse"
	 * default: when checkbox is checked value is true
	 * inverse: when checkbox is checked, value is false
	 * @param logic
	 */
	public void setLogic(String logic) throws Exception {
		if("default".equals(logic) || "inverse".equals(logic))
			this.logic=logic;
		else throw new Exception("Invalid value. Only accepts 'default' and 'inverse'");
		
		refreshValue();
	}
	
	/**
	 * gets selection logic
	 * Available values="default | inverse"
	 * default: when checkbox is checked value is true
	 * inverse: when checkbox is checked, value is false
	 * @param logic
	 */
	public String getLogic(){
		return logic;
	}
	
	final public static String VERD = "#00aa00";
	final public static String VERMELL = "#aa0000";
	final public static String TARONJA = "#ffaa00";
	
	/**
	 * Establix el color del fons del checkbox
	 * @param estil
	 */
	public void setColorFons(String estil) {
		String estilActual = getStyle() != null ? getStyle() + ";" : "";
		if (VERD.equalsIgnoreCase(estil) || VERMELL.equalsIgnoreCase(estil) || TARONJA.equalsIgnoreCase(estil)) {
			setStyle(estilActual + "background-color:" + estil);
		} else {
			setStyle("");
		}

	}
}
