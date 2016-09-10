package es.caib.zkib.events;

import es.caib.zkib.datasource.DataSource;

public class XPathRerunEvent extends XPathEvent {

	String baseXPath;

	public XPathRerunEvent(DataSource ds, String xPath) {
		super(ds, xPath);
		baseXPath = xPath;
	}
	
	public XPathRerunEvent(DataSource ds, String xPath, String baseXPath) {
		super(ds, xPath);
		this.baseXPath = baseXPath;
	}
	
	
	public String getBaseXPath() {
		return baseXPath;
	}


	public void setBaseXPath(String baseXPath) {
		this.baseXPath = baseXPath;
	}


	public String toString ()
	{
		return "XPathRerunEvent[path="+getXPath()+" basePath="+getBaseXPath()+" recursive="+recursive+"]";
	}

}
