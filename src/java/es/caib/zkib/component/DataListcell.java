package es.caib.zkib.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Listcell;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;

public class DataListcell extends Listcell implements XPathSubscriber {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3924486174016292188L;
	SingletonBinder binder = new SingletonBinder(this);
	String dateFormat = null;
	
	public DataListcell() {
		super();
	}

	public String getLabel() {
		// Nota: si es canvia el codi, canviar també a DataListcellLabelBind
		String label = null;
		if(dateFormat != null){
			Calendar calendar = (Calendar) binder.getValue();
			if (calendar!=null) {//Poden haver dates amb valor nul
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
				label = simpleDateFormat.format(calendar.getTime());
			} 
		}else{
			Object value = binder.getValue();
			if(value == null){
				label = null; //després es substitueix per ""
			}
			else if(value instanceof Boolean){
				label = ((Boolean) value).booleanValue() ? "Sí" : "No";
			}
			else if(value instanceof String){
				label = (String) value;
			} else {
				label = value.toString();
			}			
		}
		if ( label == null )
			label = super.getLabel();
		if (label == null)
			label = "";
		return label;
	}

	public DataListcell(String label) {
		super(label);
	}

	public DataListcell(String label, String src) {
		super(label, src);
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

	public void setDateFormat(String dateFormat){
		this.dateFormat = dateFormat;
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
		DataListcell clone = (DataListcell) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
}
