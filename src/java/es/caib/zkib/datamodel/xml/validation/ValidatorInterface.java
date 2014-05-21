package es.caib.zkib.datamodel.xml.validation;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.exceptions.ValidationException;

public interface ValidatorInterface
{

	public void validate (DataContext ctx)  throws Exception ;
}
