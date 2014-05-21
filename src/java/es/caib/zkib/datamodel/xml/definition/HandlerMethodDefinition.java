package es.caib.zkib.datamodel.xml.definition;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.xml.ParseException;

public class HandlerMethodDefinition extends MethodDefinition {

	String returnProperty;
	boolean returnBean = false; 

	public HandlerMethodDefinition() {
		super();
	}
	
	

	/* (non-Javadoc)
	 * @see es.caib.zkib.datasource.xml.definition.MethodDefinition#test()
	 */
	public void test(Element element) throws ParseException {
		super.test(element);
		if (getParams().length == 0)
		{
			ParameterDefinition param = new ParameterDefinition();
			param.setValue("${instance}");
			add (param);
		}
	}



	public String getReturnProperty() {
		return returnProperty;
	}



	public void setReturnProperty(String returnProperty) {
		this.returnProperty = returnProperty;
	}



	public boolean isReturnBean() {
		return returnBean;
	}



	public void setReturnBean(boolean returnBean) {
		this.returnBean = returnBean;
	}

}
