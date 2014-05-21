package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Treecell;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataTreecellDestacat extends DataTreecell implements XPathSubscriber {

	/**
	 * Extendem les cel·les del DataTreecell per afegir un bindinq que
	 * determina segons el seu valor (boolea) si la cel·la ha de tindre  
	 * l'estil css que s'especifique a la propietat classDestacat
	 * 
	 * Alejandro Usero Ruiz - 11/11/11 8:50
	 */
	private static final long serialVersionUID = 4115350210074177684L;
	

	public void setPage(Page page) {
		super.setPage(page);
		binderDestacat.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binderDestacat.setParent(parent);
	}

	public Object clone() {
		DataTreecellDestacat clone = (DataTreecellDestacat) super.clone();
		clone.binderDestacat = new SingletonBinder (clone);
		clone.binderDestacat.setDataPath(binderDestacat.getDataPath());
		return clone;
	}
	
	SingletonBinder binderDestacat =  new SingletonBinder(this);
	String classDestacat="dataTreeCelldestacada";
	
	
	public void setDestacatBoolea(String bind) {
		binderDestacat.setDataPath(bind);
	}
	
	public void setClassDestacat (String classDestacada){
		this.classDestacat=classDestacada;
	}


	public String getSclass() {
		if (binderDestacat!=null && (binderDestacat.getValue() instanceof Boolean) ) {
			if (((Boolean) binderDestacat.getValue()).booleanValue()) return classDestacat;
		}
		return super.getSclass();
	}
}
