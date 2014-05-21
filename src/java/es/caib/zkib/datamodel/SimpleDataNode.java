package es.caib.zkib.datamodel;




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

}
