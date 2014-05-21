package es.caib.zkib.debug;

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.beanutils.DynaBean;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public class PointerTree extends TreeObject {

	private JXPathContext context;
	private String pointer;

	public PointerTree(JXPathContext context, String string) {
		this.context = context;
		pointer = string;
	}

	public Vector createChildren() {
		Vector v = new Vector ();
		for (Iterator it = context.iteratePointers(pointer+"/*"); it.hasNext(); )
		{
			Pointer p = (Pointer) it.next();
			v.add(new PointerTree (context, p.asPath()));
		}
		return v;
	}

	public String getKey() {
		int i = pointer.lastIndexOf("/");
		if ( i >= 0)
			return pointer.substring(i + 1);
		else
			return pointer;
	}

	public String getValue() {
		Object obj = context.getValue(pointer);
		if (obj == null)
			return "<null>";
		else if (obj instanceof DynaBean)
			return  ((DynaBean) obj).getDynaClass().getName();
		else
			return obj.toString();
	}

}
