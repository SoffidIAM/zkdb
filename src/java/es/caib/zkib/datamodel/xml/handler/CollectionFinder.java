package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;

import javax.servlet.jsp.el.ELException;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;

public class CollectionFinder extends AbstractHandler implements FinderHandler {
	String collection;
	
	public CollectionFinder() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Returns the collection.
	 */
	public String getCollection() {
		return collection;
	}

	/**
	 * @param collection The collection to set.
	 */
	public void setCollection(String collection) {
		this.collection = collection;
	}

	public Collection find(DataContext ctx) throws Exception {
		return (Collection) Interpreter.evaluate(ctx, collection, Collection.class);
	}

	public void test(Element element) throws ParseException {
		if (collection == null)
			throw new ParseException ("Collection no especified", element);
	}

}
