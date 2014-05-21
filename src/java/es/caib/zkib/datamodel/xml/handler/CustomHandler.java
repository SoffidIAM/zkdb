package es.caib.zkib.datamodel.xml.handler;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.ParseException;

public class CustomHandler extends AbstractHandler implements PersistenceHandler {
	String className;
	PersistenceHandler handler;
	
	public CustomHandler() {
		super();
	}

	public void doInsert(DataContext node) throws Exception {
		handler.doInsert(node);
	}

	public void doDelete(DataContext node) throws Exception {
		handler.doDelete(node);
	}

	public void doUpdate(DataContext node) throws Exception {
		handler.doUpdate(node);
	}

	public boolean isSuitable(DataContext ctx) {
		if (! super.isSuitable(ctx) )
			return false;
		else
			return handler.isSuitable(ctx);
	}

	public void test(Element element) throws ParseException {
		if ( className == null)
			throw new ParseException ("className attribute not especified", element);
		try {
			Class c = Class.forName(className);
			handler = (PersistenceHandler) c.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ParseException ("class "+className+" not defined",element);
		} catch (ClassCastException e) {
			throw new ParseException ("class "+className+" does not implement PersistenceHandler",element);
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

}
