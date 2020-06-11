package es.caib.zkib.component;

import org.json.JSONObject;
import org.zkoss.xel.FunctionMapper;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelContext;
import org.zkoss.xel.XelException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

public class JSONContext implements XelContext {
	private Component c;
	private JSONObject value;

	public JSONContext(Component comp, JSONObject value) {
		this.value = value;
		c = comp;
	}

	public VariableResolver getVariableResolver() {
		return new JSONCustomVariableResolver (value, Executions.getCurrent().getVariableResolver());
	}

	public FunctionMapper getFunctionMapper() {
		return c.getPage().getFunctionMapper();
	}
}


class JSONCustomVariableResolver implements VariableResolver {

	private VariableResolver parent;
	private JSONObject value;

	public JSONCustomVariableResolver(JSONObject value, VariableResolver variableResolver) {
		this.value = value;
		this.parent = variableResolver;
	}

	public Object resolveVariable(String name) throws XelException {
		Object p;
		try {
			p = value.opt(name);
		} catch (Exception e) {
			throw new XelException ( e );
		}
		if ( p != null)
			return p;
		else
			return parent.resolveVariable(name);
	}
	
}