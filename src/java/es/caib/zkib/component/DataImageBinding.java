package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Image;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataImageBinding extends Image implements XPathSubscriber{
	
	/**
	 * Extendem el component Image per afegir un binding que
	 * determina la imatge que es mostrarà segons el seu valor.
	 * També es mostra el tooltiptext segons el valor del binding.
	 * I si té clickable=true mostra el puntero del ratolí com una ma.
	 * 
	 * Alejandro Usero Ruiz - 03/01/12 12:13
	 */
	private static final long serialVersionUID = 1L;
	SingletonBinder binder = new SingletonBinder(this);
	// Indica si és clicable (si el ratolí ha de canviar el cursor a forma de ma)
	private boolean clickable = false; 

	private boolean enableTooltip =  true;

	public DataImageBinding() {
		super();
	}

	public boolean isEnableTooltip() {
		return enableTooltip;
	}

	public void setEnableTooltip(boolean enableTooltip) {
		this.enableTooltip = enableTooltip;
	}

	public String getBind() {
		return binder.getDataPath();
	}

	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	public void onUpdate (XPathEvent event) {
		if (getPage() != null) {
			// Obtenim l'imatge que correspon al binding actual
			String srcBinding = getSrcBinding();
			if (getSrc() == null || getSrc() != null
					&& !getSrc().equals(srcBinding))
				super.setSrc(srcBinding);
			invalidate();
		}
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
		DataImageBinding clone = (DataImageBinding) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		return clone;
	}
	
	// attribut 'afegit' per indicar si és una imatge amb events 
	// (com els botons)
	public boolean isClickable() {
		return clickable;
	}

	// attribut 'afegit' per indicar si és una imatge amb events 
	// (com els botons)
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
		// Establim la classe css perquè el mouse tinga punta de fletxa
		if (clickable) super.setSclass("imageclic");
	}

	// Fem que es mostre el valor del binding com a tooltip
	public String getTooltiptext() {
		if (! enableTooltip)
			return "";
		
		String label = (String) binder.getValue();
		if ( label == null )
			label = super.getTooltiptext();
		if (label == null)
			label = "";
		return label;		
	}
	
	/* Equivalència de valor del binding amb la seua imatge */
	String[][] valorBindingEquivalImatge = null;
	
	public void setSrcBinding ( String[][] bindingImatge ) {
		this.valorBindingEquivalImatge = bindingImatge;
	}

	/* Obtenim la imatge que correspon al valor del binding actual */
	public String getSrcBinding() {
		String valorBinding =(String) binder.getValue();
		if (valorBindingEquivalImatge != null && valorBinding != null) {
			for (int i=0; i < valorBindingEquivalImatge.length; i++) {
				String [] s= valorBindingEquivalImatge[i];
				if (s.length >=2 && valorBinding.equalsIgnoreCase(s[0]))
					return s[1];				
			}
		}
		// Si no s'ha trobat cap.. retornem una imatge per defecte (interrogant)
		return "~./img/help.gif";
	}
	
	
}
