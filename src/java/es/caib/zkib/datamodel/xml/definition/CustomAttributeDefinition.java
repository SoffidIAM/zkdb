package es.caib.zkib.datamodel.xml.definition;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;

public class CustomAttributeDefinition implements DefinitionInterface {
	private String name;
	private String value;
	private String expr;
	private List<String> dependsList = new Vector<String>(3);

	public List<String> getDependsList() {
		return dependsList;
	}

	public void setDependsList(List<String> dependsList) {
		this.dependsList = dependsList;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	public String getDepends() {
		return dependsList.toString();
	}

	public void setDepends(String depends) {
		this.dependsList.add(depends);
	}

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

	public CustomAttributeDefinition() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void test(Element element) throws ParseException {
		if (name == null)
			throw new ParseException ("Name attribute is mandatory",element);
	}

	public void add (DependsDefinition def)
	{
		dependsList.add(def.getValue());
	}
}
