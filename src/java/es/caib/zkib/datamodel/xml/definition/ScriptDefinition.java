package es.caib.zkib.datamodel.xml.definition;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;

public class ScriptDefinition implements DefinitionInterface {
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

	public ScriptDefinition() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void test(Element element) throws ParseException {
		if (value == null)
			throw new ParseException ("Script not especified", element);

	}

}

