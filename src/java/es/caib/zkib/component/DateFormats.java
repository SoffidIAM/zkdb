package es.caib.zkib.component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.zkoss.util.Locales;
import org.zkoss.util.TimeZones;

public class DateFormats {
	private static final ThreadLocal<String[]> dateFormat = new ThreadLocal<String[]>();
	
	public static final String[] setThreadLocal(String[] s) {
		final String[] old = dateFormat.get();
		dateFormat.set(s);
		return old;
	}
	
	public static final String[] getThreadLocal() {
		return dateFormat.get();
	}
	

	public static DateFormat getDateFormat () {
		String[] f = getThreadLocal();
		DateFormat df;
		if (f == null)
		{
			df = DateFormat.getDateInstance(DateFormat.SHORT, Locales.getCurrent());
		} else {
			df = new SimpleDateFormat(f[0], Locales.getCurrent());
		}
		return df;
	}
	
	public static DateFormat getDateTimeFormat () {
		String[] f = getThreadLocal();
		DateFormat df;
		if (f == null)
		{
			df = DateFormat.getDateTimeInstance(DateFormat.SHORT,  DateFormat.SHORT, Locales.getCurrent());
		} else {
			df = new SimpleDateFormat(f[0]+" "+f[1], Locales.getCurrent());
		}
		df.setTimeZone(TimeZones.getCurrent());
		return df;
	}
	
	public static String getDateFormatString () {
		DateFormat df = getDateFormat();
		if (df instanceof SimpleDateFormat)
			return ((SimpleDateFormat) df).toPattern();
		else
			return "yyyy/MM/dd";
	}
	
	public static String getDateTimeFormatString () {
		DateFormat df = getDateTimeFormat();
		if (df instanceof SimpleDateFormat)
			return ((SimpleDateFormat) df).toPattern();
		else
			return "yyyy/MM/dd HH:mm";
	}
}
