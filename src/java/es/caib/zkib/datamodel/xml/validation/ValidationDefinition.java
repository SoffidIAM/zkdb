package es.caib.zkib.datamodel.xml.validation;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.DefinitionInterface;
import es.caib.zkib.exceptions.ValidationException;

public class ValidationDefinition implements DefinitionInterface, ValidatorInterface {

	List<ValidatorInterface> validators = new LinkedList<ValidatorInterface>();
	
	public void test(Element element) throws ParseException {
	}

	public void validate(DataContext ctx)  throws Exception 
	{
		for (ValidatorInterface validator: validators)
			validator.validate(ctx);
		
	}	
	
	public void add(ValidatorInterface validator) 
	{
		validators.add(validator);
	}

}
