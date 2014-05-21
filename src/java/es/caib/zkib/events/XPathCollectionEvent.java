package es.caib.zkib.events;

import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datasource.DataSource;

public class XPathCollectionEvent extends XPathEvent {
	public final static int ADDED = 1;
	public final static int DELETED = 2;
	public final static int RECREATED = 3;
	public final static int FOCUSNODE = 4;
	private int type;
	private int index;
	private DataModelCollection model;
	
	/**
	 * @return Returns the model.
	 */
	public DataModelCollection getModel() {
		return model;
	}

	/**
	 * @return Returns the max.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	public XPathCollectionEvent(DataSource ds, String xPath, int type, int index) {
		super (ds, xPath);
		Object obj = ds.getJXPathContext().getValue(xPath);
		if (obj instanceof DataModelCollection)
			this.model = (DataModelCollection) obj; 
		this.type = type;
		this.index = index;
	}
	
	public String toString ()
	{
		return "XPathCollectionEvent[path="+getXPath()+",type="+type+",index="+index+"]";
	}

}
