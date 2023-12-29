package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;

import es.caib.zkib.datamodel.DataContext;

public interface RefreshHandler {
	public Object refresh (DataContext ctx) throws Exception;
}
