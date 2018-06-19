package es.caib.zkib.datamodel.xml;

import java.io.Serializable;
import java.util.Collection;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.definition.FinderDefinition;
import es.caib.zkib.datamodel.xml.handler.FinderHandler;
import es.caib.zkib.datamodel.xml.handler.NewInstanceHandler;

public class DataNodeFinder implements es.caib.zkib.datamodel.ExtendedFinder, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FinderDefinition definition ;
	DataContext ctx;
	
	public DataNodeFinder(DataContext ctx, FinderDefinition finderDefinition) {
		this.ctx = ctx;
		this.definition = finderDefinition;
	}

	public Collection find() throws Exception {
		FinderHandler handlers [] = definition.getFinderHandlers();
		Collection c = null;
		for (int i = 0; i< handlers.length; i++)
		{
			if (handlers[i].isSuitable(ctx))
			{
				Collection c2 = handlers[i].find(ctx);
				if (c2 != null)
				{
					if ( c == null) c = c2;
					else c.addAll(c2);
				}
					
			}
		}
		return c;
	}

	public Object newInstance() throws Exception {
		NewInstanceHandler handlers [] = definition.getNewInstanceHandlers();
		for (int i = 0; i< handlers.length; i++)
		{
			if (handlers[i].isSuitable(ctx))
				return handlers[i].newInstance(ctx);
		}
		throw new RuntimeException("Not allowed to add new data");
	}

	public boolean refreshAfterCommit() {
		return definition.isRefreshAfterCommit();
	}

	public boolean findOnNewObjects() {
		return definition.isExecuteOnNewObjects();
	}

	public boolean updateBeforeParents() {
		return ! definition.isUpdateAfterParent();
	}

}
