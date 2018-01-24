package es.caib.zkib.binder.tree;

import java.io.Serializable;

public class ChildXPathQuery implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String xPath;
	Object hint;
	public ChildXPathQuery(String path, Object hint) {
		super();
		xPath = path;
		this.hint = hint;
	}
	/**
	 * @return Returns the hint.
	 */
	public Object getHint() {
		return hint;
	}
	/**
	 * @return Returns the xPath.
	 */
	public String getXPath() {
		return xPath;
	}
}
