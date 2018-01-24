package es.caib.zkib.datamodel.xml.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.handler.PersistenceHandler;
import es.caib.zkib.datamodel.xml.validation.ValidationDefinition;

public class NodeDefinition implements DefinitionInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	HashMap finders = new HashMap();
	Vector persistencers = new Vector (3);
	LinkedList<CustomAttributeDefinition> customAttributes = new LinkedList<CustomAttributeDefinition>();
	LinkedList<ValidationDefinition> validations = new LinkedList<ValidationDefinition>();
	
	public LinkedList<ValidationDefinition> getValidations() {
		return validations;
	}

	public void setValidations(LinkedList<ValidationDefinition> validations) {
		this.validations = validations;
	}


	boolean _transient;
	
	public boolean isTransient() {
		return _transient;
	}

	public void setTransient(boolean _transient) {
		this._transient = _transient;
	}

	public NodeDefinition() {
		super();
	}
	
	public PersistenceHandler [] getPersistenceHandlers ()
	{
		return (PersistenceHandler []) persistencers.toArray(new PersistenceHandler[0]);
	}
	
	public void add (PersistenceHandler handler)
	{
		persistencers.add (handler);
	}

	
	public LinkedList<CustomAttributeDefinition> getCustomAttributes() {
		return customAttributes;
	}

	public void add (CustomAttributeDefinition att)
	{
		customAttributes.add (att);
	}
	
	public CustomAttributeDefinition getCustomAttribute (String name)
	{
		for (CustomAttributeDefinition def : customAttributes)
		{
			if (def.getName().equals(name))
				return def;
		}
		return null;
	}
	
	
	public FinderDefinition getFinder (String name)
	{
		return (FinderDefinition) finders.get(name);
	}
	
	public FinderDefinition[] getFinders ()
	{
		return (FinderDefinition[]) finders.values().toArray(new FinderDefinition[0]);
	}
	
	public void add (FinderDefinition finder)
	{
		finders.put(finder.getName(), finder);
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

	public void test(Element element) throws ParseException {
		if (name == null)
			throw new ParseException ("No name especified for datanode", element);
	}
	

	public void add(ValidationDefinition validation)
	{
		validations.add(validation);
	}
}
