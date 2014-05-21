package es.caib.zkib.debug;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;
import org.zkoss.zk.ui.Component;

public class ComponentAttribute extends TreeObject {
	Component component;
	String attribute;

	public Component getComponent() {
		return component;
	}
	public void setComponent(Component component) {
		this.component = component;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getValue ()
	{
		Object value = null;
		try {
			value = BeanUtils.getProperty(component, attribute);
		} catch (IllegalAccessException e) {
			value = "No accesible :(";
		} catch (InvocationTargetException e) {
			value = "No accesible :(";
		} catch (NoSuchMethodException e) {
			value = "No accesible :(";
		}
		if (value == null)
			return "<null>";
		else
			return value.toString();
	}
	private static Vector nullVector = new Vector();
	public Vector createChildren() {
		return nullVector;
	}
	public String getKey() {
		return attribute;
	}
}
