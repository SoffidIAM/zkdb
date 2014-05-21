package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;

import es.caib.zkib.datamodel.DataContext;

public interface FinderHandler {
	public boolean isSuitable (DataContext node);
	public Collection find (DataContext ctx) throws Exception;
}
