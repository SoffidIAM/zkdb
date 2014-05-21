package es.caib.zkib.binder.list;


import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public class DataNodeComparator extends AbstractComparator {

	private DataModelCollection collection;

	public DataNodeComparator(DataModelCollection collection, String bind, boolean ascending) {
		super(bind, ascending);
		this.collection = collection;
	}

	@Override
	public JXPathContext getContext(Object o1) {
		Integer i = ( Integer) o1;
		DataModelNode dn = collection.getDataModel(i);
		JXPathContext ctx = collection.getDataSource().getJXPathContext();
		Pointer p = ctx.getPointer(dn.getXPath());
		return ctx.getRelativeContext(p);
	}

}
