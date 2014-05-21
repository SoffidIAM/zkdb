package es.caib.zkib.events;

import es.caib.zkib.datasource.DataSource;

public class XPathEvent {
	private String xPath;
	private DataSource dataSource;
	

	public XPathEvent(DataSource ds, String xPath) {
		this.dataSource = ds;
		this.xPath = xPath;
	}

	public String getXPath() {
		return xPath;
	}

	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public void send ()
	{
		getDataSource().sendEvent(this);
	}

}
