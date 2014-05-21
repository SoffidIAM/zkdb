package es.caib.zkib.events;

import es.caib.zkib.datasource.DataSource;

public class XPathRerunEvent extends XPathEvent {

	public XPathRerunEvent(DataSource ds, String xPath) {
		super(ds, xPath);
	}
	public String toString ()
	{
		return "XPathRerunEvent[path="+getXPath()+"]";
	}

}
