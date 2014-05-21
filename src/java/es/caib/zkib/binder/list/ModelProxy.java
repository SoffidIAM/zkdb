package es.caib.zkib.binder.list;

import org.zkoss.zul.ListModel;

public interface ModelProxy extends ListModel{

	public abstract String getBind(int index);
	
	public int newInstance () throws Exception; 
	
	public int getPosition (int xpathIndex);

	public abstract void unsubscribe();

}