package es.caib.zkib.datasource;

import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataModelCollection;


public class CommitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6767188280296228187L;
	
	DataModelNode data;
	
	public DataModelNode getData() {
		return data;
	}

	public CommitException() {
		super();
	}

	public CommitException(DataModelNode data, String message) {
		super(message);
		this.data = data;
	}

	public CommitException(DataModelNode data, Throwable cause) {
		super(cause);
		this.data = data;
	}

	public CommitException(DataModelNode data, String message, Throwable cause) {
		super(message, cause);
		this.data = data;
	}
	
	public void activateDataModel ()
	{
		if ( data != null )
			activateDataModel(data);
	}
	
	private void activateDataModel (DataModelNode data)
	{
		DataModelCollection listModel = data.getContainer();
		if (listModel != null)
		{
			if (data.getParent() != null)
				activateDataModel(data.getParent());
			listModel.setActiveNode(data);
		}
	}

}
