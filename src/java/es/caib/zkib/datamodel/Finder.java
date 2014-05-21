package es.caib.zkib.datamodel;

import java.util.Collection;


public interface Finder {
	/**
	 * Recuperar objetos del modelo
	 * 
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public Collection find () throws Exception;
	/**
	 * Instanciar un nuevo objeto
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object newInstance () throws Exception;

}
