package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Div;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

/**
 * @author u88683
 *
 */
public class DataDivVisible extends Div implements XPathSubscriber {
	
	/**
	 * Extendem el component Div per afegir un binding que determina si el
	 * component (i sos fills) és VISIBLE o NO comprovant el matching del
	 * binding amb valors de valorVisible i valorNoVisible [primer es comprova
	 * el de visible]
	 * 
	 * També es pot comprovar si el binding és nul (amb equivalència NULL)
	 * 
	 * Alejandro Usero Ruiz - 30/01/12 10:17
	 */
	
	private static final long serialVersionUID = 1L;
	SingletonBinder binder = new SingletonBinder(this);

	public DataDivVisible() {
		super();
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
		DataDivVisible clone = (DataDivVisible) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
	
	
	/**
	 * Element que indica si s'ha de mostrar el div quan el
	 * binding tinga el mateix valor que mostra 
	 */
	Object valorVisible;
	

	/**
	 * Element que indica si NO s'ha de mostrar el DIV quan el
	 * binding tinga el mateix valor que mostra 
	 */
	Object valorNoVisible;
	
	public Object getValorVisible() {
		return valorVisible;
	}

	public void setValorVisible(Object valorVisible) {
		this.valorVisible = valorVisible;
	}

	public Object getValorNoVisible() {
		return valorNoVisible;
	}

	public void setValorNoVisible(Object valorNoVisible) {
		this.valorNoVisible = valorNoVisible;
	}	
	

	public boolean setVisible(boolean visible) {
		return super.setVisible(visible);
	}

	public boolean isVisible() {
		Object valorBinding = binder.getValue();
		Object visible = getValorVisible();
		Object noVisible = getValorNoVisible();
		
		// cas per a valors nulls: l'atribut visible o novisible eq null
		if (valorBinding == null) {
			if ((visible!=null && "null".equalsIgnoreCase(visible.toString())) ) {
				return true; 
			}
			if ((noVisible!=null && "null".equalsIgnoreCase(noVisible.toString())) ) {
				return false; 
			}
		}
		
		if (valorBinding != null &&  (visible!=null || noVisible !=null) ) {
			
			// Mirem si és de tipus String
			if (valorBinding instanceof String) {
				String vbindingS = (String) valorBinding;
				if (visible !=null && visible instanceof String) {
					if (vbindingS.equalsIgnoreCase((String) visible))
						return true; // si match.. visible
					else
						return false; // no si match.. visible
					
				} else if (noVisible !=null || noVisible instanceof String) {
					if (vbindingS.equalsIgnoreCase((String) noVisible))
						return false; //si match.. no visible
					else
						return true; //si no match.. visible
				}
			}
			// Mirem si és de tipus Boolean
			if (valorBinding instanceof Boolean) {
				Boolean vbindingB = (Boolean) valorBinding;
				if (visible !=null && visible instanceof Boolean) {
					return vbindingB.booleanValue() == ((Boolean) visible).booleanValue();
					
				} else if (noVisible !=null && noVisible instanceof Boolean) {
					return vbindingB.booleanValue() == ((Boolean) noVisible).booleanValue();
				}
			}			

		}

		// Si arribem aquí no s'han establerts els paràmetres adequats
		return super.isVisible();
	}


}
