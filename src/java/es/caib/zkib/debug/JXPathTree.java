package es.caib.zkib.debug;

import java.util.Iterator;
import java.util.Vector;

import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public class JXPathTree extends TreeObject {
	

	private JXPathContext context;

	public JXPathTree(JXPathContext pathContext) {
		context = pathContext;
	}

	public Vector createChildren() {
		Vector v = new Vector ();
		for (Iterator it = context.iteratePointers("/*"); it.hasNext(); )
		{
			Pointer p = (Pointer) it.next();
			v.add(new PointerTree (context, p.asPath()));
		}
		return v;
	}

	public String getKey() {
		return "Data Model";
	}

	public String getValue() {
		return "";
	}

}
