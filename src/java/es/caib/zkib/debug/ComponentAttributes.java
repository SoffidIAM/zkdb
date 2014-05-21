package es.caib.zkib.debug;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.zkoss.zk.ui.Component;

public class ComponentAttributes extends TreeObject {
	Component component;

	public ComponentAttributes(Component component) {
		super();
		this.component = component;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public Vector createChildren() {
		Vector v = new Vector();
		
		Map properties = new HashMap ();
		try {
			properties = BeanUtils.describe(component);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		for (Iterator it = properties.keySet().iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			ComponentAttribute at = new ComponentAttribute();
			at.setComponent(component);
			at.setAttribute(key);
			v.add(at);
		}
		return v;
	}

	public String getKey() {
		return "Attributes";
	}

	public String getValue() {
		return "";
	}
}
