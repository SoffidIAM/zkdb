package es.caib.zkib.component;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.zkoss.xel.FunctionMapper;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelContext;
import org.zkoss.xel.XelException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

import es.caib.zkib.datamodel.DataNode;

public class TreeNodeContext implements XelContext {
	private Component c;
	private Object value;

	public TreeNodeContext(Component comp, Object value) {
		this.value = value;
		c = comp;
	}

	public VariableResolver getVariableResolver() {
		return new CustomVariableResolver (value, Executions.getCurrent().getVariableResolver());
	}

	public FunctionMapper getFunctionMapper() {
		return c.getPage().getFunctionMapper();
	}
}


class CustomVariableResolver implements VariableResolver {

	private VariableResolver parent;
	private Object value;

	public CustomVariableResolver(Object value, VariableResolver variableResolver) {
		this.value = value;
		this.parent = variableResolver;
	}

	public Object resolveVariable(String name) throws XelException {
		Object p;
		try {
			p = PropertyUtils.getProperty(value, name);
		} catch (Exception e) {
			throw new XelException ( e );
		}
		if ( p != null)
			return p;
		else
 			return parent.resolveVariable(name);
	}
	
}