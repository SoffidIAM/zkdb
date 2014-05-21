package es.caib.zkib.datamodel;

import es.caib.zkib.datasource.DataSource;

public class DataContext implements Cloneable {
	private DataNode parent;
	private DataNode current;
	private DataNodeCollection listModel;
	private DataSource dataSource;
	private String xPath;
	private Object data;
	private Object customData;

	public DataContext()
	{
	}

	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}
	/**
	 * @return Returns the listModel.
	 */
	public DataNodeCollection getListModel() {
		return listModel;
	}
	/**
	 * @return Returns the parent.
	 */
	public DataNode getParent() {
		return parent;
	}
	/**
	 * @return Returns the xPathPrefix.
	 */
	public String getXPath() {
		return xPath;
	}
	/**
	 * @return Returns the data.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @return Returns the customData.
	 */
	public Object getCustomData() {
		return customData;
	}

	/**
	 * @param customData The customData to set.
	 */
	public void setCustomData(Object customData) {
		this.customData = customData;
	}

	/**
	 * @param data The data to set.
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @param dataSource The dataSource to set.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @param listModel The listModel to set.
	 */
	public void setListModel(DataNodeCollection listModel) {
		this.listModel = listModel;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(DataNode parent) {
		this.parent = parent;
	}

	/**
	 * @param path The xPath to set.
	 */
	public void setXPath(String path) {
		xPath = path;
	}

	/**
	 * @return Returns the current.
	 */
	public DataNode getCurrent() {
		return current;
	}

	/**
	 * @param current The current to set.
	 */
	public void setCurrent(DataNode current) {
		this.current = current;
	}
	
}
