package es.caib.zkib.datamodel.xml.validation;

import bsh.EvalError;
import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.definition.DefinitionInterface;
import es.caib.zkib.datamodel.xml.definition.ScriptDefinition;
import es.caib.zkib.datasource.CommitException;

public class ScriptValidationDefinition extends ScriptDefinition implements DefinitionInterface, ValidatorInterface {


	public void validate(DataContext ctx) throws Exception {
		try {
			es.caib.zkib.datamodel.xml.Interpreter.interpret(ctx, getValue());
		} catch (EvalError e) {
			throw new CommitException(ctx.getCurrent(), e.getMessage());
		}
	}

}
