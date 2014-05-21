package es.caib.zkib.binder.tree;

import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.EventHandler;
import org.zkoss.zk.ui.metainfo.ZScript;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.component.DataListbox;
import es.caib.zkib.component.DataTree;
import es.caib.zkib.component.MasterListItem;
import es.caib.zkib.component.MasterTreeitem;
import es.caib.zkib.jxpath.JXPathContext;

public class DataTreeitemRenderer implements TreeitemRenderer {
	DataTree _tree;
	
	public DataTreeitemRenderer(DataTree tree) {
		_tree = tree;
	}

	public void render(Treeitem item, Object data) throws Exception {
		try {
			String xPath;
			
			if (data == null)
				return ;
			
			TreeModel model =  _tree.getModel();
			
			if ( ! (data instanceof TreeModelProxyNode))
				throw new UnsupportedOperationException ("Data model must be TreeModelProxy model");

			TreeModelProxyNode node = (TreeModelProxyNode) data;
			SingletonBinder binder = new SingletonBinder (item);
			binder.setDataPath(node.getPointer().asPath());
			item.setAttribute(BindContext.BINDCTX_ATTRIBUTE, binder, Component.COMPONENT_SCOPE);
			MasterTreeitem master = (MasterTreeitem) node.getHint();
			// Copiar atributos
			item.setAction(master.getAction());
			item.setCheckable(master.isCheckable());
			item.setContext(master.getContext());
			item.setDisabled(master.isDisabled());
			item.setHeight(master.getHeight());
			item.setImage(master.getImage());
			item.setLabel(master.getLabel());
			item.setLeft(master.getLeft());
			item.setMold(master.getMold());
			item.setPopup(master.getPopup());
//			item.setSrc(master.getSrc());
			item.setStyle(master.getStyle());
			item.setTooltip(master.getTooltip());
			item.setTooltiptext(master.getTooltiptext());
			item.setTop(master.getTop());
			item.setVisible(master.isVisible());
			item.setWidth(master.getWidth());
			item.setZIndex(master.getZIndex());
			item.setSclass(master.getSclass());
			item.setValue(master.getValue());
			for (Object att: master.getAttributes().keySet())
			{
				String attName = (String) att;
				item.setAttribute(attName, master.getAttribute(attName));
			}
			// Agregar eventos
			for (Object att: master.getEventHandlerNames())
			{
				String eventName = (String) att;
				ZScript script = master.getEventHandler(eventName);
				EventHandler eh = new org.zkoss.zk.ui.metainfo.EventHandler(script, null);
				item.addEventHandler(eventName, eh);
			}
			if (master.getBind() != null)
			{
				JXPathContext ctx = _tree.getDataSource().getJXPathContext().getRelativeContext(node.pointer);
				item.setValue(ctx.getValue(master.getBind()));
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
				Component clone = (Component) c1.clone();
				clone.setParent(item);
			}
			if (master.isOpen())
			{
				Events.postEvent("onTreeitemOpen", _tree, item);
			}
			Events.postEvent("onNewRow", _tree, item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private int [] getTreeItemPath (Treeitem item)
	{
		Vector v = new Vector ();
		while (item != null )
		{
			v.add(item.indexOf());
			Treechildren brothers = (Treechildren) item.getParent();
			Component c = brothers.getParent();
			if (c instanceof Treeitem)
				item = (Treeitem) c;
			else
				item = null;
		}
		int work [] = new int [v.size()];
		for (int i = 0; i < v.size(); i ++)
		{
			Integer integer = (Integer) v.get(work.length-1-i);
			work [ i ] = integer.intValue();
		}
		return work;
	}
}
