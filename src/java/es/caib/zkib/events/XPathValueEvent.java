package es.caib.zkib.events;

import es.caib.zkib.datasource.DataSource;

public class XPathValueEvent extends XPathEvent{
	public XPathValueEvent(DataSource ds, String xPath) {
		super (ds, xPath);
	}

	public String toString ()
	{
		return "XPathValueEvent[path="+getXPath()+"]";
	}

}
