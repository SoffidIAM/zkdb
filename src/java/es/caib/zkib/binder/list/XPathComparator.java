package es.caib.zkib.binder.list;

import java.sql.Date;
import java.util.Calendar;
import java.util.Comparator;

import org.apache.commons.collections.ComparatorUtils;

import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public class XPathComparator extends AbstractComparator {

	private JXPathContext ctx;

	public XPathComparator(JXPathContext ctx, String bind, boolean ascending) {
		super (bind, ascending);
		this.ctx = ctx;
	}

	@Override
	public JXPathContext getContext(Object o1) {
		if (o1 instanceof Pointer)
		{
			Pointer p1 = (Pointer) o1;
			return ctx.getRelativeContext(p1);
		}
		else
		{
			return ctx.getRelativeContext(ctx.getPointer((String) o1));
		}
	}
}
