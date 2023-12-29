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

public class ScriptRefresh extends AbstractHandler implements RefreshHandler, DefinitionInterface {
	// FinderScriptDefinition script;
	String value;
	
	public ScriptRefresh() {
		super();
	}

	public Object refresh(DataContext ctx) {
		try {
			return Interpreter.interpret(ctx, value);
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
