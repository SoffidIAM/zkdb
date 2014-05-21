package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;
import java.util.Vector;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.MethodDefinition;
import es.caib.zkib.datamodel.xml.definition.ParameterDefinition;

public class EJBFinder extends AbstractEJBHandler implements FinderHandler {
	public String method;
	private MethodDefinition finderMethod = new MethodDefinition ();
	
	public EJBFinder() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Collection find(DataContext ctx) throws Exception {
		Object obj = invokeMethod(ctx, finderMethod);
		if (obj == null)
			return null;
		else if (obj instanceof Collection)
			return (Collection) obj;
		else
		{
			Vector v = new Vector (1);
			v.add (obj);
			return v;
		}
	}

	public void test(Element element) throws ParseException {
		finderMethod.test(element);
	}


	/**
	 * @return Returns the method.
	 */
	public String getMethod() {
		return finderMethod.getMethod();
	}

	/**
	 * @param method The method to set.
	 */
	public void setMethod(String method) {
		finderMethod.setMethod(method);
	}

	/**
	 * @return Returns the params.
	 */
	public ParameterDefinition[] getParams() {
		return finderMethod.getParams();
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.datasource.xml.definition.MethodDefinition#add(es.caib.zkib.datasource.xml.definition.ParameterDefinition)
	 */
	public void add(ParameterDefinition param) {
		finderMethod.add(param);
	}

	
}
