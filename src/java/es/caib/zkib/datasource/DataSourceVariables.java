/**
 * 
 */
package es.caib.zkib.datasource;

import java.util.HashMap;

import es.caib.zkib.jxpath.Variables;

/**
 * @author u07286
 *
 */
public class DataSourceVariables implements Variables {
	HashMap variables = new HashMap();
	/**
	 * 
	 */
	public DataSourceVariables() {
		super();
	}

	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Variables#isDeclaredVariable(java.lang.String)
	 */
	public boolean isDeclaredVariable(String varName) {
		return variables.containsKey(varName);
	}

	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Variables#getVariable(java.lang.String)
	 */
	public Object getVariable(String varName) {
		return variables.get(varName);
	}

	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Variables#declareVariable(java.lang.String, java.lang.Object)
	 */
	public void declareVariable(String varName, Object value) {
		variables.put(varName, value);
	}

	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.Variables#undeclareVariable(java.lang.String)
	 */
	public void undeclareVariable(String varName) {
		variables.remove(varName);
	}

}
