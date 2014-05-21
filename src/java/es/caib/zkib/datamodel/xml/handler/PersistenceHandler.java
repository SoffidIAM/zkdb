package es.caib.zkib.datamodel.xml.handler;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.XmlDataNode;

public interface PersistenceHandler {
	public boolean isSuitable (DataContext node);
	public void doInsert (DataContext node) throws Exception;
	public void doDelete (DataContext node) throws Exception;
	public void doUpdate (DataContext node) throws Exception;
}
