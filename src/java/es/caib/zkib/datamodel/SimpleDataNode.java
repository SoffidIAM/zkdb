package es.caib.zkib.datamodel;

import java.lang.reflect.InvocationTargetException;

public class SimpleDataNode extends DataNode {

	public SimpleDataNode(DataContext ctx) {
		super(ctx); 
	}

	protected void doInsert () throws Exception
	{
		throw new Exception ("Insert not allowed");
	}

	protected void doUpdate () throws Exception
	{
		throw new Exception ("Update not allowed");
	}

	
	protected void doDelete () throws Exception
	{
		throw new Exception ("Delete not allowed");
	}

	public Object getParentId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCurrentId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getChildProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object loadParentObject() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
