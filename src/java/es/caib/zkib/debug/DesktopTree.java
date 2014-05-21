package es.caib.zkib.debug;

import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;

public class DesktopTree extends TreeObject {
	Desktop desktop;
	
	
	public DesktopTree(Desktop desktop) {
		super();
		this.desktop = desktop;
	}

	public Vector createChildren() {
		Vector v = new Vector();
		for (Iterator it = desktop.getPages().iterator(); it.hasNext();)
		{
			v.add ( new PageTree ( (Page) it.next()));
		}
		return v;
	}

	public String getKey() {
		// TODO Auto-generated method stub
		return "Desktop";
	}

	public String getValue() {
		// TODO Auto-generated method stub
		return "";
	}

}
