package es.caib.zkib.datamodel.xml.definition;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;

public class DependsDefinition implements DefinitionInterface
{
	String value;
	
	
	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public void test(Element element) throws ParseException {
	}


}
