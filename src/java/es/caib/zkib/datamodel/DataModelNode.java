package es.caib.zkib.datamodel;

import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;


public interface DataModelNode {

	public DataModelCollection getListModel(String name);

	public DataModelCollection getContainer();

	public void prepareCommit() throws CommitException;

	public void commit();

	public void refresh();

	public void delete();
	
	public void undelete ();

	public void update();
	
	public void updateChildren ();
	
	public boolean isCommitPending ();

	public boolean isDeleted();

	public boolean isNew();

	public boolean isUpdated();
	
	public DataModelNode getParent ();
	
	public String getXPath ();
	
	public DataSource getDataSource ();
	
}