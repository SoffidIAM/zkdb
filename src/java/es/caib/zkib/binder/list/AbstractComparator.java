package es.caib.zkib.binder.list;

import java.sql.Date;
import java.util.Calendar;
import java.util.Comparator;

import org.apache.commons.collections.ComparatorUtils;

import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public abstract class AbstractComparator implements Comparator {

	private String xPath;
	private int multiply;
	private boolean ascending;

	public AbstractComparator( String bind, boolean ascending) {
		this.xPath = bind;
		this.multiply = ascending ? 1 : -1;
		this.ascending = ascending;
	}
	
	public abstract JXPathContext getContext (Object o1);

	public int compare(Object o1, Object o2) {
		Object v1 = getContext(o1).getValue(xPath);
		Object v2 = getContext(o2).getValue(xPath);
		
		if (v1 == null && v2 == null)
			return 0;
		if (v1 == null)
			return - multiply;
		if (v2 == null)
			return + multiply;
		if ( v1 instanceof java.util.Date && v2 instanceof java.util.Date)
		{
			long l = ((java.util.Date)v2).getTime()-((java.util.Date)v1).getTime() ;
			if ( l == 0) return 0;
			else if ( l > 0 ) return + multiply;
			else return - multiply;
		}
		if ( v1 instanceof java.sql.Date && v2 instanceof java.sql.Date)
		{
			long l = ((java.sql.Date)v2).getTime()-((java.sql.Date)v1).getTime() ;
			if ( l == 0) return 0;
			else if ( l > 0 ) return + multiply;
			else return - multiply;
		}
		if ( v1 instanceof Calendar && v2 instanceof Calendar)
		{
			long l = ((Calendar)v2).getTime().getTime()-((Calendar)v1).getTime().getTime() ;
			if ( l == 0) return 0;
			else if ( l > 0 ) return + multiply;
			else return - multiply;
		}
		if ( v1 instanceof Long && v2 instanceof Long)
		{
			long l = ((Long)v2).longValue() - ((Long)v1).longValue();
			if ( l == 0) return 0;
			else if ( l > 0 ) return + multiply;
			else return - multiply;
		}
		if ( v1 instanceof Integer && v2 instanceof Integer)
		{
			long l = ((Integer)v2).longValue() - ((Integer)v1).longValue();
			if ( l == 0) return 0;
			else if ( l > 0 ) return + multiply;
			else return - multiply;
		}
		return multiply * v1.toString().compareTo(v2.toString());
	}

}
