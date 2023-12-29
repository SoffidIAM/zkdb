package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.ParseException;

public class CustomRefresh extends AbstractHandler implements RefreshHandler {
	String className;
	RefreshHandler handler;
	
	public CustomRefresh() {
		super();
	}

	public void test(Element element) throws ParseException {
		if ( className == null)
			throw new ParseException ("className attribute not especified", element);
		try {
			Class c = Class.forName(className);
			handler = (RefreshHandler) c.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ParseException ("class "+className+" not defined",element);
		} catch (ClassCastException e) {
			throw new ParseException ("class "+className+" does not implement RefreshHandler",element);
		} catch (InstantiationException e) {
			throw new ParseException ("Cannot instantiate "+className,element, e);
		} catch (IllegalAccessException e) {
			throw new ParseException ("Cannot instantiate "+className,element, e);
		}
	}

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



	public Object refresh(DataContext ctx) throws Exception {
		return handler.refresh(ctx);
	}

}
