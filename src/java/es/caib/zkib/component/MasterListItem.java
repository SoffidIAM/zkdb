package es.caib.zkib.component;

import org.zkoss.zk.scripting.Namespace;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

public class MasterListItem extends Listitem implements IdSpace {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7982352127626817466L;
	private String bind;
	private Listbox theListbox;
	
	
	public Listbox getTheListbox() {
		return theListbox;
	}


	public void setTheListbox(Listbox theListbox) {
		this.theListbox = theListbox;
	}


	public MasterListItem() {
		super();
	}


	@Override
	public Namespace getNamespace() {
		if (theListbox != null)
			return theListbox.getNamespace();
		else
			return super.getNamespace();
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


	/* (non-Javadoc)
	 * @see org.zkoss.zul.Listitem#insertBefore(org.zkoss.zk.ui.Component, org.zkoss.zk.ui.Component)
	 */
	@Override
	public boolean insertBefore(Component child, Component insertBefore) {
		return super.insertBefore(child, insertBefore);
	}

	
}
