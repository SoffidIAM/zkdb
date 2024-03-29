package es.caib.zkib.datamodel.xml.definition;

import java.io.Serializable;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;

public class AttributeDefinition implements DefinitionInterface, Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String value;
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
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

	public AttributeDefinition() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void test(Element element) throws ParseException {
		if (name == null)
			throw new ParseException ("Name attribute is mandatory",element);
	}

}
