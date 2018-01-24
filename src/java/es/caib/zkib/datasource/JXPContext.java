/**
 * 
 */
package es.caib.zkib.datasource;


import java.io.Serializable;

import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.ClassFunctions;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;
import es.caib.zkib.jxpath.Variables;
import es.caib.zkib.jxpath.ri.JXPathContextReferenceImpl;

public class JXPContext extends JXPathContextReferenceImpl implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DataSource ds = null;
	Pointer pointer;
	JXPContext parentContext = null;
	
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.JXPathContext#getVariables()
	 */
	public Variables getVariables() {
		if (parentContext == null)
			return ds.getVariables ();
		else
			return parentContext.getVariables();
	}

	private String composeXPath (String xPath)
	{
		if (pointer == null)
			return xPath;
		else if (! xPath.startsWith("[") && ! xPath.startsWith("/") && xPath.length() > 0)
			return pointer.asPath() + "/" + xPath;
		else if (pointer.asPath().endsWith("/") && xPath.startsWith("/"))
			return pointer.asPath() + xPath.substring(1);
		else
			return pointer.asPath() + xPath;
	}
	
	public void setValue(String xpath, Object value) {
		super.setValue(xpath, value);
		onUpdate (xpath);
	}

	
	protected void onUpdate (String xpath) {
		if (parentContext != null)
			parentContext.onUpdate (composeXPath(xpath));
		if (ds != null)
			ds.sendEvent(new XPathValueEvent (ds, xpath));		
	}
	
	protected void onReRunXPath (String xpath)
	{
		if (parentContext != null)
			parentContext.onReRunXPath (composeXPath(xpath));
		if (ds != null)
			new XPathRerunEvent (ds, xpath).send();
	}
	
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.ri.JXPathContextReferenceImpl#getPointer(java.lang.String)
	 */
	public Pointer getPointer(String xpath) {
//		String newPath = xpath.replaceAll("/\\./", "/");
		Pointer p = super.getPointer(xpath);
		if ( p == null)
			return p;
		else
			return new DSPointer ( this, p);
	}

	public JXPContext( JXPContext parentContext,  Pointer pointer) {
		super(parentContext, pointer.getNode());
		this.pointer = pointer;
		this.parentContext = parentContext;
		setFunctions(new ClassFunctions(StringFunctions.class, "soffid"));
		setFactory(JXPathFactory.getInstance());
	} 
	
	public JXPContext (DataSource ds, Object obj) {
		super(null, obj);
		this.ds = ds;
		setFunctions(new ClassFunctions(StringFunctions.class, "soffid"));
		setFactory(JXPathFactory.getInstance());
	} 

    public JXPathContext getRelativeContext(Pointer pointer) {
        return new JXPContext(this, pointer);
    }

	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.ri.JXPathContextReferenceImpl#createPath(java.lang.String)
	 */
	public Pointer createPath(String xpath) {
		return new DSPointer (this, super.createPath(xpath));
	}

	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.ri.JXPathContextReferenceImpl#createPathAndSetValue(java.lang.String, java.lang.Object)
	 */
	public Pointer createPathAndSetValue(String xpath, Object value) {
		return new DSPointer (this, super.createPathAndSetValue(xpath, value));
	}
	
	public DataSource getDataSource ()
	{
		if (parentContext == null)
			return ds;
		else
			return parentContext.getDataSource();
	}
}

