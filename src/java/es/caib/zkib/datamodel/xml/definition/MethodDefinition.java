package es.caib.zkib.datamodel.xml.definition;

import java.util.Vector;

import javax.servlet.jsp.el.ELException;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;


public class MethodDefinition implements DefinitionInterface {
	private String method;
	private Vector params = new Vector ();
        private Vector catchs = new Vector ();
	
	public MethodDefinition() {
		super();
	}
	
	public void add (ParameterDefinition param)
	{
		params.add(param);
	}

        public void add (CatchDefinition param)
        {
                catchs.add(param);
        }

	/**
	 * @return Returns the method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method The method to set.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return Returns the params.
	 */
	public ParameterDefinition[] getParams() {
		return (ParameterDefinition[]) params.toArray(new ParameterDefinition [0]);
	}

	/*
        * @return Returns the catchs.
        */
       public CatchDefinition[] getCatchs() {
               return (CatchDefinition[]) catchs.toArray(new CatchDefinition [0]);
       }

        public void test(Element element) throws ParseException {
		if (method == null)
			throw new ParseException ("Method name not especified", element);
	}
	
	public Object [] evaluateParams(DataContext ctx) 
	{
		Object result [] = new Object [params.size()];
		for (int i = 0; i < result.length;i ++)
		{
			String expr = ((ParameterDefinition) params.get(i)).getValue();
			try {
				result [ i ] = Interpreter.evaluate(ctx, expr);
			} catch (ELException e) {
				throw new RuntimeException("Error evaluating "+expr, e);
			}
		}
		return result;
		
	}
	
}
