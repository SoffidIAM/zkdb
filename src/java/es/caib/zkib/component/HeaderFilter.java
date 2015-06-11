package es.caib.zkib.component;

public interface HeaderFilter {
	public String getBind ();
	public void appendFilter(StringBuffer filter);
}
