package es.caib.zkib.datamodel.xml;

import javax.servlet.jsp.el.ELException;

import es.caib.zkib.datamodel.DataContext;

public class VariableResolver implements javax.servlet.jsp.el.VariableResolver {
	DataContext ctx;
	
	public VariableResolver(DataContext ctx) {
		super();
		this.ctx = ctx;
	}

	public Object resolveVariable(String pName) throws ELException {
		if ("instance".equals(pName))
			return ctx.getData();
		if ("self".equals(pName))
			return ctx.getCurrent();
		if ("parent".equals(pName))
			return ctx.getParent();
		if ("datasource".equals(pName))
			return ctx.getDataSource();
		if (ctx.getDataSource() != null &&
			ctx.getDataSource().getVariables().isDeclaredVariable(pName))
			return ctx.getDataSource().getVariables().getVariable(pName);
		return null;
	}

}
