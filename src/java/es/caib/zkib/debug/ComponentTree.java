package es.caib.zkib.debug;

import java.util.Vector;

import org.zkoss.zk.ui.Component;

import es.caib.zkib.datasource.DataSource;

public class ComponentTree extends TreeObject {
	org.zkoss.zk.ui.Component component;
	
	public ComponentTree(Component component) {
		super();
		this.component = component;
	}

	public Vector createChildren() {
		Vector v = new Vector();
		
		v.add( new ComponentAttributes (component));
		v.add( new ComponentChildren (component));
		if (component instanceof DataSource)
		{
			DataSource ds = (DataSource) component;
			v.add ( new JXPathTree (ds.getJXPathContext()));
		}
		
		return v;
	}

	public String getKey() {
		return component.getDefinition().getName();
	}

	public String getValue() {
		return component.getId();
	}

	public org.zkoss.zk.ui.Component getComponent() {
		return component;
	}

	public void setComponent(org.zkoss.zk.ui.Component component) {
		this.component = component;
	}

}
