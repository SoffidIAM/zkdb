package es.caib.zkib.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Tree;

import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class DataTreecellImage extends DataTreecell implements XPathSubscriber {

	/**
	 * Extendem les cel·les del DataTreecell per afegir un bindinq que
	 * determina segons el seu valor la imatge que s'ha de mostrar en
	 * la branca de l'arbre actual
	 * 
	 * Alejandro Usero Ruiz - 27/04/12 8:19
	 */
	private static final long serialVersionUID = 4115350210074177684L;
	

	public void setPage(Page page) {
		super.setPage(page);
		binderImage.setPage(page);
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binderImage.setParent(parent);
	}

	public Object clone() {
		DataTreecellImage clone = (DataTreecellImage) super.clone();
		clone.binderImage= new SingletonBinder (clone);
		clone.binderImage.setDataPath(binderImage.getDataPath());
		return clone;
	}
	
	SingletonBinder binderImage =  new SingletonBinder(this);

	
	public void setImageBind(String bind) {
		binderImage.setDataPath(bind);
	}
	
	private String checkImg () {
		String valor = (String)binderImage.getValue();
		DataTree arbre = (DataTree)getTree(); 
		String myImg = arbre!=null ? arbre.getImageBinding(valor) : null;
		
		if (myImg!=null && !myImg.equals(super.getImage()))
			super.setImage(myImg);	
		
		return myImg;
	}

	@Override
	public String getImage() {
		if (binderImage!=null) {
			String img = checkImg();
			if (img!=null) return checkImg(); else return "";
		}
		return super.getImage();
	}

	@Override
	public String getSrc() {
		return this.getImage();
	}
	
	public DataTree getTree() {
		for (Component n = this; (n = n.getParent()) != null;) {
			if (n instanceof MasterTreeitem)
				return ((MasterTreeitem) n).getTheTree();
			else if (n instanceof Tree)
				return (DataTree) n;
		}
		return null;
	}

	@Override
	public void onUpdate(XPathEvent event) {
		checkImg ();		
		super.onUpdate(event);
	}
	
	

}
