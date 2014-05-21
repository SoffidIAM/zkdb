package es.caib.zkib.datamodel.xml.handler;

import java.util.Vector;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;
import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.AttributeDefinition;

public class NewBeanInstance extends AbstractHandler implements NewInstanceHandler {
	Vector attributes = new Vector ();
	String className;
	
	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	public NewBeanInstance() {
		super();
	}

	public Object newInstance(DataContext ctx) {
		Object obj;
		try {
			ClassLoader cl = getClass().getClassLoader();
			Class clazz = cl.loadClass(className);
			obj = clazz.newInstance();
			if (! attributes.isEmpty())
			{
				WrapDynaBean wrapper = new WrapDynaBean(obj);
				for (int i = 0; i < attributes.size(); i++)
				{
					AttributeDefinition attr = (AttributeDefinition) attributes.get(i);
					DynaProperty prop = wrapper.getDynaClass().getDynaProperty(attr.getName());
					if (prop == null)
						throw new RuntimeException ("Unknown property "+attr.getName());
					Object value = Interpreter.evaluate(ctx, attr.getValue(), prop.getType());
					wrapper.set(attr.getName(), value);
				}
			}
			return obj;
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	public void test(Element element) throws ParseException {
		if (className == null)
			throw new ParseException ("className is mandatory", element);
	}
	
	public AttributeDefinition [] getAttributes ()
	{
		return (AttributeDefinition[]) attributes.toArray(new AttributeDefinition[0]);
	}

	public void add (AttributeDefinition attribute)
	{
		attributes.add(attribute);
	}
}
