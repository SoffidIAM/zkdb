package es.caib.zkib.exceptions;

import es.caib.zkib.component.DataModel;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datasource.AbstractDataSource;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;

public class ValidationException extends CommitException {
	String xpath;
	
	public String getXpath() {
		return xpath;
	}

	public ValidationException(DataModelNode data, String xpath,
			String message) {
		super(data, message);
		this.xpath = xpath;
	}

	public void activateDataModel ()
	{
		super.activateDataModel();
		DataSource dataSource = getData().getDataSource();
		if (dataSource != null && dataSource instanceof AbstractDataSource)
		{
			((AbstractDataSource) dataSource).setFocus(xpath);
		}
	}

}
