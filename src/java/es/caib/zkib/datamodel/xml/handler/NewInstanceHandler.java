package es.caib.zkib.datamodel.xml.handler;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.CustomData;

public interface NewInstanceHandler {
	public boolean isSuitable (DataContext node);
	public Object newInstance (DataContext ctx);
}
