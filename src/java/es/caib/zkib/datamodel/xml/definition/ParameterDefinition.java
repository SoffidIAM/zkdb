package es.caib.zkib.datamodel.xml.definition;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;

public class ParameterDefinition implements DefinitionInterface {
	private String value;
	
	public ParameterDefinition() {
		super();
		// TODO Auto-generated constructor stub
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

	public void test(Element element) throws ParseException {
		return;
		
	}

}
