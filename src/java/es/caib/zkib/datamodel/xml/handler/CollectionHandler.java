package es.caib.zkib.datamodel.xml.handler;

import java.util.Collection;

import javax.servlet.jsp.el.ELException;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.DataNodeCollection;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;

public class CollectionHandler extends AbstractHandler implements
		PersistenceHandler {
	String collection;

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

	public CollectionHandler() {
		super();
	}

	private void consolidateCollection (DataContext ctx) throws ELException
	{
		DataNodeCollection model = ctx.getListModel();
		Collection coll = (Collection) Interpreter.evaluate(ctx.getParent().getDataContext(), collection, Collection.class);
		coll.clear();
		for (int i = 0; i < model.getSize(); i++)
		{
			DataNode node = (DataNode) model.getDataModel(i);
			if (node != null && !node.isDeleted())
				coll.add(node.getDataContext().getData());
		}
	}
	
	public void doInsert(DataContext ctx) throws ELException {
		Collection coll;
//		coll = (Collection) Interpreter.evaluate(ctx.getParent().getDataContext(), collection, Collection.class);
//		if (!coll.contains(ctx.getData()))
//			coll.add(ctx.getData());
		consolidateCollection(ctx);
		ctx.getParent().update();
	}

	public void doDelete(DataContext ctx) throws ELException {
//		Collection coll;
//		coll = (Collection) Interpreter.evaluate(ctx.getParent().getDataContext(), collection, Collection.class);
//		coll.remove(ctx.getData());
		consolidateCollection(ctx);
		ctx.getParent().update();
	}

	public void doUpdate(DataContext ctx) throws ELException {
		consolidateCollection(ctx);
		ctx.getParent().update();
	}

	public void test(Element element) throws ParseException {
		if (collection == null)
			throw new ParseException ("Collection no especified", element);
	}

}
