package es.caib.zkib.component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.Div;

public class Embed extends Div implements IdSpace {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String src;
	boolean created = false;

	public String getSrc() {
		return src;
	}

	@SuppressWarnings("unchecked")
	public void setSrc(String src) {
		this.src = src;
		if (created)
		{
			for (Component child: new LinkedList<Component> ((List<Component>)getChildren()))
			{
				detachChildren(child);
			}
			createComponents ();
		}
	}
	
	private void detachChildren(Component c) {
		Component child = c.getFirstChild();
		while (child != null) {
			detachChildren(child);
			child = c.getFirstChild();
		}
		c.detach();
	}


	@Override
	public Object clone() {
		Embed e = new Embed();
		e.created = true;
		e.setSrc(getSrc());
		return e;
	}

	public void onCreate ()
	{
		createComponents ();
	}

	private void createComponents() {
		if (src != null)
		{
			Executions.getCurrent().createComponents(src, this, new HashMap<String, String>());
		}
	}

}
