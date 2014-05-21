package es.caib.zkib.datamodel.xml.handler;

import org.w3c.dom.Element;

import org.zkoss.zk.ui.UiException;

import bsh.EvalError;
import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.CustomData;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.DefinitionInterface;

public class NewInstanceScript extends AbstractHandler 
	implements NewInstanceHandler, DefinitionInterface {
	String value;
	
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public NewInstanceScript() {
		super();
	}

	public Object newInstance(DataContext ctx) {
		try {
			return Interpreter.interpret(ctx, value);
		} catch (EvalError e) {
			throw new UiException(e);
		}
	}

	public void test(Element element) throws ParseException {
		if (value == null)
			throw new ParseException ("No script specified", element);
		
	}

}
