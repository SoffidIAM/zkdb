package es.caib.zkib.component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.zkoss.xel.ExpressionFactory;
import org.zkoss.xel.Expressions;
import org.zkoss.xml.HTMLs;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.event.TreeDataEvent;
import org.zkoss.zul.event.TreeDataListener;
import org.zkoss.zul.impl.XulElement;

import es.caib.zkdb.yaml.Yaml2Json;
import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.binder.tree.FullTreeModelProxy;
import es.caib.zkib.binder.tree.TreeModelProxy;
import es.caib.zkib.binder.tree.TreeModelProxyNode;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datasource.ChildDataSourceImpl;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.JXPContext;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Variables;

public class DataTree2 extends XulElement implements XPathSubscriber,
	BindContext, DataSource, AfterCompose {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JSONArray finders = null;
	FullTreeModelProxy model;
	SingletonBinder treeBinder = new SingletonBinder (this);
    ChildDataSourceImpl dsImpl = null;
	private String selectedItemXPath;
	private int[] selectedItem;
    boolean autocommit = false;
    boolean _updateValueBinder = true;
	private transient TreeDataListener _dataListener;
	private static final String ADD_CHILDREN_EVENT = "onAddChildren"; //$NON-NLS-1$
	private static final String SELECT_EVENT = "onSelect"; //$NON-NLS-1$
	private static final String CLIENT_ACTION_EVENT = "onClientAction"; //$NON-NLS-1$
	boolean enablefilter = true;
	int sortDirection = +1;
	boolean sortable = true;
	String header = "";
	boolean footer = true;
	String maxheight = "";
	String objects = null;
	int openLevels = -1;
	private ExpressionFactory expf;
	
	public DataTree2 () {
		setSclass("datatree");
	}
    public String getDataPath() {
        return treeBinder.getDataPath();
    }
    
    public String getSelectedItemXPath () {
    	return selectedItemXPath;
    }

    public void setDataPath(String bind) throws Exception {
    	treeBinder.setDataPath(bind);
        applyModel();
    }

    @Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final DataTree2 self = this;
		final String uuid = self.getUuid();		

		wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.datatree2.Datatree2\"")
		.write(self.getOuterAttrs()).write(self.getInnerAttrs())
		.write(">")
		.write("</div>");
	}

	
	@Override
	public String getInnerAttrs() {
		StringBuffer sb = new StringBuffer ( super.getInnerAttrs() );
		
		HTMLs.appendAttribute(sb, "header", header);
		HTMLs.appendAttribute(sb, "enablefilter", enablefilter);
		HTMLs.appendAttribute(sb, "sortable", sortable);
		HTMLs.appendAttribute(sb, "sortDirection", sortDirection);
		HTMLs.appendAttribute(sb, "footer", footer);
		HTMLs.appendAttribute(sb, "maxheight", maxheight);

		return sb.toString();

	}


	public void setFinders(String columns) throws Exception {
		try {
			String f = new Yaml2Json().transform(columns);
			finders = new JSONArray(f);
			applyModel();
		} catch (IOException e) {
			throw new UiException("Unable to parse JSON descriptor "+columns);
		}
	}

    /**
     * 
     */
	
	private void applyModel() throws Exception {
		if (treeBinder.getDataPath() != null && finders != null)
		{
			FullTreeModelProxy lm = new FullTreeModelProxy(treeBinder); 

			for (int i = 0; i < finders.length(); i++)
			{
				JSONObject o = finders.optJSONObject(i);
				lm.addChildXPathQuery(
						o.getString("path"),
						o);
			}
			TreeModel oldListModel = getModel ();
			if ( oldListModel != null && oldListModel instanceof TreeModelProxy)
			{
				((TreeModelProxy) oldListModel).unsubscribe ();
			}

			setModel ( lm );
	
			if (dsImpl == null)
				dsImpl = new ChildDataSourceImpl ();
			
			dsImpl.setDataSource(treeBinder.getDataSource());
			dsImpl.setRootXPath(treeBinder.getXPath());
	
			int[] path = getSelectedItem();
			if (path != null && path.length > 0)
			{
				selectedItemXPath = lm.getXPath(path);
				dsImpl.setXPath(selectedItemXPath);
				
			}
		}
	}
	

	public TreeModel getModel() {
		return model;
	}

	public void setData (JSONObject obj) {
		StringWriter sb = new StringWriter();
		JSONWriter writer = new JSONWriter(sb);
		dump (obj,  writer, -1, new LinkedList<Integer>());
		response("setData", new AuInvoke(this, "setData", sb.toString()));
	}
	
	private void dump(JSONObject node, JSONWriter writer, int levels, LinkedList<Integer> pos) {
		writer.object();
		writer.key("position");
		writer.array();
		for (Integer p: pos) {
			writer.value(p);
		}
		writer.endArray();
		String type = node.optString("type");
		if ( type != null)
		{
			for ( int n = 0; n < finders.length(); n++)
			{
				JSONObject finder = finders.optJSONObject(n);
				if (type.equals( finder.optString("path")))
				{
					String icon = finder.optString("icon");
					if (icon != null && ! icon.isEmpty())
					{
						writer.key("icon");
						writer.value(getDesktop().getExecution().encodeURL(icon));
					}
					String value = finder.optString("value");
					String template = finder.optString("template");
					if (value != null && ! value.isEmpty())
					{
						Object v = node.opt(value);
						writer.key("value");
						writer.value(v);
					} else if (template != null)
					{
						if (expf == null)
							expf = Expressions.newExpressionFactory(null);
						Object v = expf.evaluate(new JSONContext (this, node), template, String.class);
						writer.key("html");
						writer.value(v);
					}
					if (finder.optBoolean("leaf"))
					{
						writer.key("leaf");
						writer.value(true);
					}
					if (node.has("collapsed"))
					{
						writer.key("collapsed");
						writer.value(node.optBoolean("collapsed"));
					} else if (finder.optBoolean("collapsed"))
					{
						writer.key("collapsed");
						writer.value(true);
					}
					String tail = finder.optString("tail");
					if (tail != null) {
						writer.key("tail");
						writer.value(tail);
					}
				}
			}
		}
		
		if (levels != 0 && node.has("children")) {
			writer.key("children");
			writer.array();
			JSONArray children = node.optJSONArray("children");
			int num = children.length();
			for (int i = 0; i < num; i++)
			{
				JSONObject child = children.getJSONObject(i);
				pos.add(i);
				dump(child, writer, levels - 1, pos );
				pos.removeLast();
			}
			writer.endArray();
		}
		writer.endObject();
	}

	public void setModel(FullTreeModelProxy lm) {
		if (lm != null) {
			if (this.model != lm) {
				if (this.model != null) {
					this.model.removeTreeDataListener(_dataListener);
				}
				this.model = lm;

				TreeModelProxyNode root = (TreeModelProxyNode) lm.getRoot();
				StringWriter sb = new StringWriter();
				JSONWriter writer = new JSONWriter(sb);
				dump (lm, root, writer, openLevels, new LinkedList<Integer>());
				response("setData", new AuInvoke(this, "setData", sb.toString()));
				initDataListener();
			}
		} else if (this.model != null) {
			this.model.removeTreeDataListener(_dataListener);
			this.model = null;
			response("setData", new AuInvoke(this, "setData", "{}"));
		}
	}

	
	private void dump(FullTreeModelProxy lm, TreeModelProxyNode node, JSONWriter writer, int levels, LinkedList<Integer> pos) {
		writer.object();
		writer.key("position");
		writer.array();
		for (Integer p: pos) {
			writer.value(p);
		}
		writer.endArray();
		if ( ! node.getLocalPath().equals("."))
		{
			String path = node.getLocalPath();
			int l = path.lastIndexOf("[");
			if (l > 0) path = path.substring(0, l);
			for ( int n = 0; n < finders.length(); n++)
			{
				JSONObject finder = finders.optJSONObject(n);
				if (path.equals( finder.optString("path")))
				{
					String icon = finder.optString("icon");
					if (icon != null && ! icon.isEmpty())
					{
						writer.key("icon");
						writer.value(getDesktop().getExecution().encodeURL(icon));
					}
					String value = finder.optString("value");
					String template = finder.optString("template");
					if (value != null && ! value.isEmpty())
					{
						Object v = JXPContext.newContext(node.getValue()).getValue(value);
						writer.key("value");
						writer.value(v);
					} else if (template != null)
					{
						if (expf == null)
							expf = Expressions.newExpressionFactory(null);
						Object v = expf.evaluate(new TreeNodeContext (this, node.getValue()), template, String.class);
						writer.key("html");
						writer.value(v);
					}
					if (finder.optBoolean("leaf"))
					{
						writer.key("leaf");
						writer.value(true);
					}
					if (finder.optBoolean("collapsed"))
					{
						writer.key("collapsed");
						writer.value(true);
					}
					String tail = finder.optString("tail");
					if (tail != null) {
						writer.key("tail");
						writer.value(tail);
					}
				}
			}

		}
		
		if (levels != 0) {
			writer.key("children");
			writer.array();
			int num = lm.getChildCount(node);
			for (int i = 0; i < num; i++)
			{
				TreeModelProxyNode child = (TreeModelProxyNode) lm.getChild(node, i);
				pos.add(i);
				dump(lm, child, writer, levels - 1, pos );
				pos.removeLast();
			}
			writer.endArray();
		}
		writer.endObject();
	}
	
	/** Initializes _dataListener and register the listener to the model
	 */
	private void initDataListener() {
		if (_dataListener == null)
			_dataListener = new TreeDataListener() {
				public void onChange(TreeDataEvent event) {
					onTreeDataChange(event);
				}

			};

		model.addTreeDataListener(_dataListener);
	}

	private void onTreeDataChange(TreeDataEvent event){
		int pos[] = model.getPath(model.getRoot(), event.getParent());
		int pos2[] = Arrays.copyOf(pos, pos.length+1);
		//if the treeparent is empty, render tree's treechildren
		TreeModelProxyNode node = (TreeModelProxyNode) event.getParent();
		int indexFrom = event.getIndexFrom();
		int indexTo = event.getIndexTo();
		Object parent;
		switch (event.getType()) {
		case TreeDataEvent.INTERVAL_ADDED:
			for(int i=indexFrom;i<=indexTo;i++)
			{
				pos2[pos.length] = i;
				onTreeDataInsert(node, pos2);
			}
			break;
		case TreeDataEvent.INTERVAL_REMOVED:
			for(int i=indexTo;i>=indexFrom;i--)
			{
				pos2[pos.length] = i;
				onTreeDataRemoved(node, pos2);
			}
			break;
		case TreeDataEvent.CONTENTS_CHANGED:
			for(int i=indexFrom;i<=indexTo;i++)
			{
				pos2[pos.length] = i;
				onTreeDataContentChange(node, pos2);
			}
			break;
		}
	}

	private void onTreeDataInsert( TreeModelProxyNode parent, int[] pos) {
        try {
        	TreeModelProxyNode node = model.getTreeModelProxyNode(pos);
        	StringWriter sb = new StringWriter();
        	LinkedList<Integer> posl = new LinkedList<Integer>();
        	for (int p: pos) posl.add(p);
        	dump (model, node, new JSONWriter(sb), 
        			pos.length > openLevels? 
        				0: 
        				openLevels - pos.length, 
        			posl );
            response("insert_"+posToString(pos), new AuInvoke(this, "addBranch", sb.toString()));
        } catch (Exception e) {
            throw new UiException(e);
        }
	}
	
	
	private void onTreeDataContentChange(TreeModelProxyNode parent, int pos[]) {
        try {
        	TreeModelProxyNode node = model.getTreeModelProxyNode(pos);
        	StringWriter sb = new StringWriter();
        	LinkedList<Integer> posl = new LinkedList<Integer>();
        	for (int p: pos) posl.add(p);
        	dump (model, node, new JSONWriter(sb), 0, posl);
            response("update_"+posToString(pos), new AuInvoke(this, "updateRow", sb.toString()));
        } catch (Exception e) {
            throw new UiException(e);
        }
	}
	
	static int msgNumber = 0 ;
	private void onTreeDataRemoved(TreeModelProxyNode parent, int [] pos) {
        try {
            response("remove_"+posToString(pos), new AuInvoke(this, "deleteRow", new JSONArray(pos).toString()));
        } catch (Exception e) {
            throw new UiException(e);
        }
	}
	
	private String posToString(int[] pos) {
		StringBuffer sb = new StringBuffer();
		for (int p: pos) {
			if (sb.length() > 0) sb.append(".");
			sb.append(p);
		}
		return sb.toString();
	}
	public int[] getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedIndex(int[] selectedIndex) {
		response("setSelected", new AuInvoke(this, "setSelected", new JSONArray(selectedIndex).toString()));
		this.selectedItem = selectedIndex;
		updateDataSource();
	}

    private void updateDataSource() {
    	if (model != null && dsImpl != null)
    	{
    		if (selectedItem == null)
    		{
    			selectedItemXPath = null;
    			dsImpl.setXPath(selectedItemXPath);
    		}
    		else
    		{
    			selectedItemXPath = model.getXPath(selectedItem);
    			dsImpl.setXPath(selectedItemXPath);
    		}
    	}
	}
    
	public JXPathContext getJXPathContext() {
        if (dsImpl == null)
            return null;
        else
            return dsImpl.getJXPathContext();
    }

    EventListener onSelectListener = null;

    public void subscribeToExpression(String path, XPathSubscriber subscriber) {
        dsImpl.subscribeToExpression(path, subscriber);
        enableOnSelectListener();
    }

	private void enableOnSelectListener() {
		if (onSelectListener == null) {
            onSelectListener = new OnSelectListener();

            this.addEventListener("onSelect", onSelectListener);

        }
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#smartUpdate(java.lang.String,
     *      java.lang.String)
     */
    public void smartUpdate(String attr, String value) {
        if ("selectedIndex".equals(attr) || "select".equals(attr)) {
            updateDataSource();
        }
        super.smartUpdate(attr, value);
    }

    public String getXPath() {
        return treeBinder.getXPath();
    }

    public void unsubscribeToExpression(String xpath, XPathSubscriber subscriber) {
        dsImpl.unsubscribeToExpression(xpath, subscriber);
    }

    public void autocommit() {
        if (isAutocommit()) {
            try {
                commit();
            } catch (UnsupportedOperationException e) {
                // Ignorar
            }
        }
    }

    public void commit() throws UiException {
        try {
            if (treeBinder != null
                    && treeBinder.getDataSource() != null)
            	treeBinder.getDataSource().commit();
        } catch (CommitException e) {
            throw new UiException(e);
        }
    }

    /**
     * @return Returns the autocommit.
     */
    public boolean isAutocommit() {
        return autocommit;
    }

    /**
     * @param autocommit
     *                The autocommit to set.
     */
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

	public String addNew (String path)
	{
		if (autocommit) commit ();
		
		try {
			TreeModelProxyNode node = model.getTreeModelProxyNode(selectedItem);
			String parentPath = node.getPointer().asPath();
			if (parentPath.endsWith("/")) 
				parentPath = parentPath.substring(0, parentPath.length()-1);
			if (path.startsWith("/"))
				path = path.substring(1);
			
			Object coll = treeBinder.getJXPathContext().getValue(parentPath+"/"+path);
			
			if ( coll == null || ! (coll instanceof DataModelCollection))
				throw new UiException("Only a data model can be added without a value");
			DataModelCollection collection = (DataModelCollection) coll;
			DataModelNode nouNode = collection.newInstance(); // Propaguem event onchange
			
			// Seleccionem el nou element a l'arbre (u88683):
			String nouXPath = nouNode.getXPath();
			// Comprovem que item != null (nodes sense pare)
			int[] index = Arrays.copyOf(selectedItem, selectedItem.length + 1);
			TreeModelProxyNode[] children = node.getChildren();
			for (int i = 0; i < children.length; i++)
			{
				if (children[i].getValue() == nouNode)
				{
					index[selectedItem.length] = i;
					setSelectedIndex(index);
					break;
				}
			}
			return nouXPath;
		} catch (ClassCastException e) {
			throw new UiException ("Path "+path+" does not referes a data collection");
		} catch (Exception e) {
			throw new UiException (e);
		}
	}

	public void delete ()
	{
		if (autocommit) commit ();
		
		if (selectedItem != null && selectedItem.length > 0)
		{
			TreeModelProxyNode node = model.getTreeModelProxyNode(selectedItem);
			Object obj = node.getValue();
			if (obj == null)
				throw new UiException ("Unable to delete object "+obj);
			if ( obj instanceof DataModelNode)
			{
				( (DataModelNode) obj).delete();
				if (autocommit) commit ();
			}
			else
			{
				XPathUtils.removePath(model.getBinder().getDataSource(), node.getPointer().asPath());
			}
			int[] sel = Arrays.copyOf(selectedItem, selectedItem.length-1);
			setSelectedIndex(sel);
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see es.caib.seycon.net.web.zul.datasource.ChildDataSourceImpl#getVariables()
     */
    public Variables getVariables() {
        return dsImpl.getVariables();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#clearSelection()
     */
    public void clearSelection() {
        selectedItemXPath = null;
        if (dsImpl != null)
            dsImpl.setXPath(null);
        selectedItem = new int[0];
        response("clearSelection", new AuInvoke(this, "clearSelection"));
    }

    public boolean isCommitPending() {
        if (dsImpl == null)
            return false;
        else
            return dsImpl.isCommitPending();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.zkoss.zul.Listbox#onInitRender()
     */
    public void onInitRender() {
    }

    public void setPage(Page page) {
        super.setPage(page);
        treeBinder.setPage(page);
    }

    public void setParent(Component parent) {
        super.setParent(parent);
        treeBinder.setParent(parent);
    }

    public Object clone() {
        DataTree2 clone = (DataTree2) super.clone();
        clone.treeBinder = new SingletonBinder(clone);
        clone.treeBinder.setDataPath(treeBinder.getDataPath());
        return clone;
    }

    private final class OnSelectListener implements EventListener {
		private static final long serialVersionUID = 1L;

		public boolean isAsap() {
		    return true;
		}

		public void onEvent(org.zkoss.zk.ui.event.Event arg0) {// NOTHING
		                                                        // TO DO
		}
	}

	public String getRootPath() {
		return dsImpl.getRootPath();
	}
	

	public void afterCompose() {
	}

    public DataSource getDataSource() {
        return treeBinder.getDataSource();
    }

    public void sendEvent(XPathEvent event) {
        dsImpl.sendEvent(event);
    }

	public void onUpdate(XPathEvent event) {
        if (event instanceof XPathRerunEvent) {
       		setSelectedIndex(new int[0]);
        }
	}

	public Command getCommand(String cmdId) {
		if (SELECT_EVENT.equals(cmdId))
			return _onSelectCommand;
		if (CLIENT_ACTION_EVENT.equals(cmdId))
			return _onClientActionCommand;
		if (ADD_CHILDREN_EVENT.equals(cmdId))
			return _onAddChildrenCommand;
		
		return super.getCommand(cmdId);
	}

	private static Command _onSelectCommand  = new ComponentCommand (SELECT_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTree2 table = (DataTree2) request.getComponent();
			JSONArray a = new JSONArray(request.getData()[0] );
			int []selectedItem = new int[ a.length() ];
			for ( int pos = 0; pos < a.length(); pos ++)
				selectedItem[pos] = a.getInt(pos);
			table.selectedItem = selectedItem;
			table.updateDataSource();
			Events.postEvent(new Event("onSelect", table, table.selectedItem));
		}
		
	};

	private static Command _onAddChildrenCommand  = new ComponentCommand (ADD_CHILDREN_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTree2 table = (DataTree2) request.getComponent();
			JSONArray a = new JSONArray(request.getData()[0] );
			int []selectedItem = new int[ a.length() ];
			LinkedList<Integer> selectedItemList = new LinkedList<Integer>();
			for ( int pos = 0; pos < a.length(); pos ++)
			{
				selectedItem[pos] = a.getInt(pos);
				selectedItemList.add ( a.getInt(pos));
			}
			
			TreeModelProxyNode node = table.model.getTreeModelProxyNode(selectedItem);
			StringWriter sb = new StringWriter();
			JSONWriter writer = new JSONWriter(sb);
			table.dump (table.model, node, writer, 1, selectedItemList);
			table.response("setData", new AuInvoke(table, "addChildren", sb.toString()));
		}
		
	};

	private static Command _onClientActionCommand  = new ComponentCommand (CLIENT_ACTION_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTree2 table = (DataTree2) request.getComponent();
			String[] data = request.getData();
			String event = data[0];
			Object[] newData = new Object [data.length-1];
			for (int i = 1; i < newData.length; i++)
				newData[i] = data[i+1];
			Events.postEvent(new Event(event, table, newData));
		}
		
	};

	public boolean isEnablefilter() {
		return enablefilter;
	}
	public void setEnablefilter(boolean enablefilter) {
		this.enablefilter = enablefilter;
	}
	public int getSortDirection() {
		return sortDirection;
	}
	public void setSortDirection(int sortDirection) {
		this.sortDirection = sortDirection;
	}
	public boolean isFooter() {
		return footer;
	}
	public void setFooter(boolean footer) {
		this.footer = footer;
		smartUpdate("footer", Boolean.toString(footer));
	}
	public String getMaxheight() {
		return maxheight;
	}
	public void setMaxheight(String maxheight) {
		this.maxheight = maxheight;
		smartUpdate("maxheight", maxheight);
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getObjects() {
		return objects;
	}
	public void setObjects(String objects) {
		this.objects = objects;
	}
	public int getOpenLevels() {
		return openLevels;
	}
	public void setOpenLevels(int openLevels) {
		this.openLevels = openLevels <= 0 ? -1: openLevels;
	}
	public boolean isSortable() {
		return sortable;
	}
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}
}
