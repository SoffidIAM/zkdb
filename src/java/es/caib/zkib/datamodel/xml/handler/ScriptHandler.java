package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;
import java.util.Vector;

import org.w3c.dom.Element;

import bsh.EvalError;

import org.zkoss.zk.ui.UiException;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.DeleteMethodDefinition;
import es.caib.zkib.datamodel.xml.definition.DeleteScriptDefinition;
import es.caib.zkib.datamodel.xml.definition.InsertMethodDefinition;
import es.caib.zkib.datamodel.xml.definition.InsertScriptDefinition;
import es.caib.zkib.datamodel.xml.definition.UpdateMethodDefinition;
import es.caib.zkib.datamodel.xml.definition.UpdateScriptDefinition;

public class ScriptHandler extends AbstractHandler implements
		PersistenceHandler {
	private InsertScriptDefinition insertMethod;
	private UpdateScriptDefinition updateMethod;
	private DeleteScriptDefinition deleteMethod;
	private String exception ;
	

	public ScriptHandler() {
		super();
	}

	public void doInsert(DataContext node) {
		try {
			if (insertMethod != null)
				Interpreter.interpret(node, insertMethod.getValue());
		} catch (EvalError e) {
			throw new UiException(e);
		}
	}

	public void doDelete(DataContext node) {
		try {
			if (deleteMethod != null)
			Interpreter.interpret(node, deleteMethod.getValue());
		} catch (EvalError e) {
			throw new UiException(e);
		}
	}

	public void doUpdate(DataContext node) {
		try {
			if (updateMethod != null)
				Interpreter.interpret(node, updateMethod.getValue());
		} catch (EvalError e) {
			throw new UiException(e);
		}
	}

	public void test(Element element) throws ParseException {
		if (exception != null)
			throw new ParseException (exception, element);
	}


	public void add (InsertScriptDefinition method) throws ParseException
	{
		if (insertMethod != null)
			exception = "Only one insert method is allowed";
		insertMethod = method;
	}

	public void add (DeleteScriptDefinition method) throws ParseException
	{
		if (deleteMethod != null)
			exception = "Only one delete method is allowed";
		deleteMethod = method;
	}
	

	public void add (UpdateScriptDefinition method) throws ParseException
	{
		if (updateMethod != null)
			exception = "Only one update method is allowed";
		updateMethod = method;
	}
	
	
}
