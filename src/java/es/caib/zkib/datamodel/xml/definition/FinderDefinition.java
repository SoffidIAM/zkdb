package es.caib.zkib.datamodel.xml.definition;

import java.io.Serializable;
import java.util.Vector;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.handler.FinderHandler;
import es.caib.zkib.datamodel.xml.handler.NewInstanceHandler;

public class FinderDefinition implements DefinitionInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	String type;
	boolean refreshAfterCommit = false;
	boolean executeOnNewObjects = false;

	Vector finderHandlers = new Vector ();
	Vector instanceHandler = new Vector ();
	
	
	public FinderDefinition() {
		super();
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
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	public void add (FinderHandler handler)
	{
		finderHandlers.add (handler);
	}
	
	public FinderHandler[] getFinderHandlers ()
	{
		return (FinderHandler []) finderHandlers.toArray(new FinderHandler [0]);
	}
	
	public void add (NewInstanceHandler handler)
	{
		instanceHandler.add (handler);
	}
	
	public NewInstanceHandler[] getNewInstanceHandlers ()
	{
		return (NewInstanceHandler []) instanceHandler.toArray(new NewInstanceHandler [0]);
	}

	public void test(Element element) throws ParseException {
		if (name == null)
			throw new ParseException ("Name not especified for finder ", element);
		if (type == null)
			throw new ParseException ("Type not especified for finder ", element);
		if (finderHandlers.isEmpty())
			throw new ParseException ("No finder handler especified ", element);
	}
	
	public void setRefreshAfterCommit(boolean v)
	{
		refreshAfterCommit = v;
	}
	
	public boolean isRefreshAfterCommit ()
	{
		return refreshAfterCommit;
	}


	public boolean isExecuteOnNewObjects() {
		return executeOnNewObjects;
	}

	public void setExecuteOnNewObjects(boolean executeOnNewObjects) {
		this.executeOnNewObjects = executeOnNewObjects;
	}


}
