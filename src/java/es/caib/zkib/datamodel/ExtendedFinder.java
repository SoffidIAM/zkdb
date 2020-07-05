package es.caib.zkib.datamodel;

public interface ExtendedFinder extends Finder {

	/**
	 * 
	 */
	public boolean refreshAfterCommit() ;
	
	/**
	 * 
	 */
	public boolean findOnNewObjects() ;
	
	/**
	 * 
	 */
	public boolean updateBeforeParents() ;

	Object loadParentObject(DataContext ctx) throws Exception;

	String getChildProperty();

	String getIdProperty();

	String getParentProperty();
	
}
