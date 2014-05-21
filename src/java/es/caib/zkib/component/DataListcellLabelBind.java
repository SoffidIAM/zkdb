package es.caib.zkib.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;

import es.caib.zkib.binder.SingletonBinder;

/**
 *	DataListcellLabelBind - Alejandro Usero Ruiz - 20 d'agost de 2012
 *
 *	- Classe filla de DataListCell que ens permet fer un binding tant
 *    en el seu value (com ja fa DataListcell) com a la seua label
 *    que pot ésser un altre binding diferent.
 *    
 * @author u88683
 *
 */
public class DataListcellLabelBind extends DataListcell {

	
	private static final long serialVersionUID = 1L;
	
	public void setPage(Page page) {
		super.setPage(page);
		binderLabel.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binderLabel.setParent(parent);
	}

	public Object clone() {
		DataListcellLabelBind clone = (DataListcellLabelBind) super.clone();
		clone.binderLabel = new SingletonBinder (clone);
		clone.binderLabel.setDataPath(binderLabel.getDataPath());
		return clone;
	}	
	
	SingletonBinder binderLabel =  new SingletonBinder(this);
	
	
	public void setLabelBind(String bind) {
		binderLabel.setDataPath(bind);
	}
	
	
	public String getLabel() {
		// Fem lo mateix que al pare (però al binder de label)
		String label = null;
		
		if (binderLabel!=null) {
			// Fem lo mateix que al pare però al binderLabel
			if(dateFormat != null){
				Calendar calendar = (Calendar) binderLabel.getValue();
				if (calendar!=null) {//Poden haver dates amb valor nul
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
					label = simpleDateFormat.format(calendar.getTime());
				} 
			}else{
				Object value = binderLabel.getValue();
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
		}
		
		// Si no s'ha obtingut res al binding.. retornem la del pare
		if (label == null)
			return super.getLabel();
		else 
			return label;
	}
	
	
	
	

}
