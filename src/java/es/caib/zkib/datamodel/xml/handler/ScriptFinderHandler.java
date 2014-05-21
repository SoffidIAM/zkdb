package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;
import java.util.Vector;

import org.w3c.dom.Element;

import bsh.EvalError;

import org.zkoss.zk.ui.UiException;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.DefinitionInterface;
import es.caib.zkib.datamodel.xml.definition.FinderScriptDefinition;

public class ScriptFinderHandler extends AbstractHandler implements FinderHandler, DefinitionInterface {
	// FinderScriptDefinition script;
	String value;
	
	public ScriptFinderHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Collection find(DataContext ctx) {
		try {
			Object obj = Interpreter.interpret(ctx, value);
			if (obj == null)
				return null;
			else if (obj instanceof Collection)
				return (Collection) obj;
			else {
				Vector v = new Vector (1);
				v.add (obj);
				return v;
			}
		} catch (EvalError e) {
			throw new UiException(e);
		}
	}


	public void test(Element element) throws ParseException {
		if (value == null)
			throw new ParseException ("Script not especified", element);
	}

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


}
