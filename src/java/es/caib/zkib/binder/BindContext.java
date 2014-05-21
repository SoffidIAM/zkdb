package es.caib.zkib.binder;

import es.caib.zkib.datasource.DataSource;

public interface BindContext {

	public final String BINDCTX_ATTRIBUTE = "bindctx";
	public final String BINDCTX_ATTRIBUTE_LABEL = "bindctxLabel";

	/**
	 * DataSource que sirve de contexto al bind de los componentes internos
	 * 
	 * @return
	 */
	public DataSource getDataSource ();
	
	/**
	 * XPath que sirve de contexto a los componentes internos
	 * 
	 * @return
	 */
	public String getXPath ();
}