package es.caib.zkib.component;

import es.caib.zkib.datasource.AbstractDataSource;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.events.XPathRerunEvent;

public class BeanDataSource extends AbstractDataSource {

	/**
	 * Versió 1.1:  Alejandro Usero Ruiz - 13/07/2011
	 *   - Si el primer node del fitxer XML és un comentari, l'ignorem
	 *     fins que trobem un fill que no siga comentari.
	 *     
	 * Versió 1.2: 20-08-2012
	 * 	 - Fem que si no troba el resource (xml) emprant zk
	 *     (getDesktop().getWeapp()) empre el classloader de la classe 
	 * 	
	 */
	private static final long serialVersionUID = 6138399623413047624L;
	private Object bean;
	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
		getDsImpl().setData(bean);
	}

	private transient Object datanode = null;
	
	public BeanDataSource() {
		super();
	}

	public Object getData() {
		return bean;
	}

	/**
	 * @return Returns the src.
	 */
	public void commit() throws CommitException {
		throw new UnsupportedOperationException ("commit");
		
	}

	public boolean isCommitPending() {
		return false;
	}

	public String getRootPath() {
		return "/";
	}

}
