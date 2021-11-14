package es.caib.zkib.binder.list;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.ListitemRendererExt;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.SmartEvents;
import es.caib.zkib.component.DataCombobox;
import es.caib.zkib.component.DataListbox;
import es.caib.zkib.component.MasterListItem;

public class DataListItemRenderer implements ListitemRenderer, Serializable, ListitemRendererExt {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DataListbox _listbox;
	DataCombobox _combobox;
	
	public DataListItemRenderer(DataListbox listbox) {
		_listbox = listbox;
	}

	public void render(Listitem item, Object data) throws Exception {
		if (data == null) return;
		try {
			String xPath;
			
			int i = item.getIndex();
			ListModel model = null;
			MasterListItem master = null;
			model = _listbox.getModel();
			master = _listbox.getMasterListItem();
			
			if (master == item) // This can happen when the paging event is processed before finishing onInitRender
				return;
			
			if (model != null && model instanceof ModelProxy )
				xPath = (( ModelProxy )model).getBind(i);
			else
				xPath = "["+(i+1)+"]";

			SingletonBinder binder = new SingletonBinder(item);
			binder.setDataPath(xPath);
			item.setAttribute(BindContext.BINDCTX_ATTRIBUTE, binder, Component.COMPONENT_SCOPE);
			
			if (master.getBind() != null && binder.getJXPathContext() != null)
				item.setValue(binder.getJXPathContext().getValue(master.getBind()));
			// Agregar los hijos
			while (item.getChildren().size() > 0)
			{
				Component c = (Component) item.getChildren().get(0);
				detachChildren(c);
			}
			Iterator it1 = master.getChildren().iterator();
			Listhead listhead = item.getListbox().getListhead();
			Iterator headersIterator = listhead == null? Collections.emptyList().iterator():
										listhead.getChildren().iterator();
			while ( it1.hasNext() )
			{
				Component c1 = (Component) it1.next();
				Component head = (Component) (headersIterator.hasNext() ? headersIterator.next(): null);
				if (head != null && !head.isVisible())
				{
					new Listcell().setParent(item);
				}
				else
				{
					Component clone = (Component) c1.clone();
					clone.setParent(item);
					if (clone instanceof IdSpace)
					{
						for (String v: new LinkedList<String>((Set<String>)clone.getNamespace().getVariableNames()))
						{
							clone.getNamespace().unsetVariable(v, true);
						}
					}
				}
			}
			SmartEvents.postEvent("onNewRow", _listbox, item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void detachChildren(Component c) {
		Component child = c.getFirstChild();
		while (child != null) {
			detachChildren(child);
			child = c.getFirstChild();
		}
		c.detach();
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

	public Listitem newListitem(Listbox listbox) {
		return null;
	}

	public Listcell newListcell(Listitem item) {
		return null;
	}

	public int getControls() {
		return 0;
	}
	
}
