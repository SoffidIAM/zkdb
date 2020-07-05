package es.caib.zkib.datamodel;

import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;


public interface DataModelCollection {

	public DataModelNode getDataModel (int index);

	public int getSize();

	public void prepareCommit() throws CommitException;

	public void commit();

	public int indexOf(DataModelNode data);
	
	public void setActiveNode (DataModelNode data);

	public void refresh() throws Exception;

	public void refreshAfterCommit();

	public DataModelNode newInstance() throws Exception;

	public void onDelete (DataModelNode data);

	public void onUndelete (DataModelNode data);

	public void onUpdate (DataModelNode data);

	public DataSource getDataSource ();
	
	public boolean isDirty();
	
	public boolean isInProgress ();
	
	public void updateProgressStatus() throws Exception;

	public void cancel ();

	public Finder getFinder();

	public boolean updateBeforeParent();
}