package es.caib.zkib.component;

import org.zkoss.zk.scripting.Namespace;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Vbox;

public class MasterTreeitem extends Treeitem implements AfterCompose {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7982352127626817466L;
	private String bind;
	private String path;
	private DataTree theTree;
	Long openLevels;
	
	public Long getOpenLevels() {
		return openLevels;
	}


	public void setOpenLevels(Long openLevels) {
		this.openLevels = openLevels;
	}


	/* (non-Javadoc)
	 * @see org.zkoss.zul.Treeitem#setParent(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void setParent(Component parent) {
		if (parent instanceof DataTree)
		{
			theTree = (DataTree) parent;
		}
		else
			super.setParent(parent);
	}


	public MasterTreeitem() {
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
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}


	/**
	 * @param path The path to set.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public void afterCompose() {
		if (theTree != null)
			theTree.addMasterTreeItem(this);
	}


	public DataTree getTheTree() {
		return theTree;
	}
}

