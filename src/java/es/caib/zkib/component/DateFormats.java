package es.caib.zkib.component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;

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
			String formatPattern =
				    DateTimeFormatterBuilder.getLocalizedDateTimePattern(
				        FormatStyle.SHORT, 
				        null, 
				        IsoChronology.INSTANCE, 
				        Locales.getCurrent());
			formatPattern = formatPattern.replaceAll("\\byy\\b", "yyyy");
			df = new SimpleDateFormat(formatPattern);
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
			String formatPattern =
				    DateTimeFormatterBuilder.getLocalizedDateTimePattern(
				        FormatStyle.SHORT, 
				        FormatStyle.SHORT, 
				        IsoChronology.INSTANCE, 
				        Locales.getCurrent());
			formatPattern = formatPattern.replaceAll("\\byy\\b", "yyyy");
			df = new SimpleDateFormat(formatPattern);
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

	public static String getTimeFormatString() {
		String[] f = getThreadLocal();
		if (f == null)
			return "HH:mm";
		else
			return f[1];
	}
}
