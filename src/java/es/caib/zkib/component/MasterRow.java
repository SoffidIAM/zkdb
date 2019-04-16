package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import org.zkoss.zk.scripting.Namespace;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Rows;

public class MasterRow extends DataRow implements IdSpace, AfterCompose {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4028732866230257053L;
	public boolean isMaster  = true;
	private Grid theGrid;
	
	public Grid getTheGrid() {
		return theGrid;
	}

	public void setTheGrid(Grid theGrid) {
		this.theGrid = theGrid;
	}

	public MasterRow() {
		super();
	}

	public void setParent(Component parent) {
		if (isMaster && parent != null && parent instanceof DataGrid)
		{
			theGrid = (Grid) parent;
			((DataGrid) parent).setMasterRow(this);
		}
		else
			super.setParent(parent);
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.AbstractComponent#redraw(java.io.Writer)
	 */
	public void redraw(Writer out) throws IOException {
		if (!isMaster) super.redraw(out);
	}

	protected void unlock () {
		isMaster = false;
	}
	
	
	@Override
	public Namespace getNamespace() {
		if (getParent() != null)
			return getParent().getNamespace();
		else if (theGrid != null)
			return theGrid.getNamespace();
		else
			return super.getNamespace();
	}

	private final IdSpace getRealSpaceOwner() {
		if (getParent() != null)
			return getParent().getSpaceOwner();
		else if (theGrid != null)
			return theGrid.getSpaceOwner();
		else
			return getPage();
	}

	/** Returns a component of the specified ID in the same ID space.
	 * Components in the same ID space are called fellows.
	 *
	 * <p>Unlike {@link #getFellowIfAny}, it throws an exception if not found.
	 *
	 * @exception ComponentNotFoundException is thrown if
	 * this component doesn't belong to any ID space
	 */
	public Component getFellow(String compId) {
		Component c = super.getFellowIfAny(compId);
		if (c != null)
			return c;
		final IdSpace idspace = getRealSpaceOwner();
		if (idspace == null)
			throw new ComponentNotFoundException("This component doesn't belong to any ID space: "+this);
		return idspace.getFellow(compId);
	}
	/** Returns a component of the specified ID in the same ID space, or null
	 * if not found.
	 * <p>Unlike {@link #getFellow}, it returns null if not found.
	 */
	public Component getFellowIfAny(String compId) {
		Component c = super.getFellowIfAny(compId);
		if (c != null)
			return c;
		final IdSpace idspace = getRealSpaceOwner();
		return idspace == null ? null: idspace.getFellowIfAny(compId);
	}
	/** Returns all fellows in this ID space.
	 * The returned collection is readonly.
	 * @since 3.0.6
	 */
	public Collection getFellows() {
		final IdSpace idspace = getRealSpaceOwner();
		return idspace == null ? Collections.EMPTY_LIST: idspace.getFellows();
	}

	public void afterCompose() {
		setParent (new Rows());
	}

}
