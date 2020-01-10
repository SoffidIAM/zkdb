package es.caib.zkib.binder.list;

import java.io.Serializable;
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.SmartEvents;
import es.caib.zkib.component.DataCombobox;
import es.caib.zkib.component.DataListbox;
import es.caib.zkib.component.MasterComboItem;
import es.caib.zkib.component.MasterListItem;

public class DataComboitemRenderer implements ComboitemRenderer, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DataCombobox _combobox;

	public DataComboitemRenderer(DataCombobox combobox) {
		_combobox = combobox;
	}

	public void render(Comboitem item, Object data) throws Exception {
		try {
			String xPath;
			
			int i = _combobox.getItems().indexOf(item);
			ListModel model = null;
			MasterComboItem master = null;
			model =  _combobox.getModel();
			master = _combobox.getMasterComboItem();
			
			if (model != null && model instanceof ModelProxy )
				xPath = (( ModelProxy )model).getBind(i);
			else
				xPath = "["+(i+1)+"]";

			SingletonBinder binder = new SingletonBinder(item);
			binder.setDataPath(xPath);
			item.setAttribute(BindContext.BINDCTX_ATTRIBUTE, binder, Component.COMPONENT_SCOPE);
			
			if (master.getBind() != null)
				item.setValue(binder.getJXPathContext().getValue(master.getBind()));
			
			if (master.getLabelBind() != null)
			{
				Object v = binder.getJXPathContext().getValue(master.getLabelBind());
				if (v != null)
					item.setLabel(v.toString());
				else
					item.setLabel("");
			}

			// Agregar los hijos
			while (item.getChildren().size() > 0)
			{
				Component c = (Component) item.getChildren().get(0);
				c.setParent(null);
			}
			Iterator it1 = master.getChildren().iterator();
			while ( it1.hasNext() )
			{
				Component c1 = (Component) it1.next();
//				duplicateComponent(c1, item);
				Component clone = (Component) c1.clone();
				clone.setParent(item);
			}
			SmartEvents.postEvent("onNewRow", item, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private Component duplicateComponent (Component master, Component parent)
	{
//		InstanceDefinition definition = (InstanceDefinition) master.getDefinition();
//		Component replica = definition.newInstance (master.getPage());
		Component replica = (Component) master.clone();
		replica.setParent(parent);
		
		applyData (replica, master);
		
		return replica;
	}
	
	private void applyData (Component component, Component master)
	{
//		InstanceDefinition definition = (InstanceDefinition) master.getDefinition();
//		definition.applyProperties(component);
		for (Iterator it = master.getChildren().iterator(); it.hasNext(); )
		{
			duplicateComponent((Component) it.next(), component);
		}
		
	}
	
}
