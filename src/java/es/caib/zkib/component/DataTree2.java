package es.caib.zkib.component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import es.caib.zkib.datamodel.DataNodeCollection;
import es.caib.zkib.datasource.ChildDataSourceImpl;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.JXPContext;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathNotFoundException;
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
	private int[] selectedItem = new int[0];
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
	String foldbar = "/img/foldBar.svg";
	String foldbackgroud = "/img/foldBackground.svg";
	String foldunfold = "/img/foldUnfold.svg";

	private ExpressionFactory expf;
	private String columns;
	
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
		
		HTMLs.appendAttribute(sb, "columns", columns);
		HTMLs.appendAttribute(sb, "header", header);
		HTMLs.appendAttribute(sb, "enablefilter", enablefilter);
		HTMLs.appendAttribute(sb, "sortable", sortable);
		HTMLs.appendAttribute(sb, "sortDirection", sortDirection);
		HTMLs.appendAttribute(sb, "footer", footer);
		HTMLs.appendAttribute(sb, "maxheight", maxheight);
		HTMLs.appendAttribute(sb, "foldBar", getDesktop().getExecution().encodeURL(foldbar));
		HTMLs.appendAttribute(sb, "foldBackground", getDesktop().getExecution().encodeURL(foldbackgroud));
		HTMLs.appendAttribute(sb, "foldUnfold", getDesktop().getExecution().encodeURL(foldunfold));

		return sb.toString();

	}

	public void setColumns(String columns) {
		try {
			this.columns = new Yaml2Json().transform(columns);
		} catch (IOException e) {
			throw new UiException("Unable to parse JSON descriptor "+columns);
		}
		
		smartUpdate("columns", this.columns);
	}

	public void setFinders(String columns) throws Exception {
		try {
			String f = new Yaml2Json().transform(columns);
			finders = new JSONArray(f);
			applyModel();
		} catch (IOException e) {
			throw new UiException("Unable to parse JSON descriptor "+columns, e);
		}
	}

    /**
     * 
     */
	
	private void applyModel() throws Exception {
		if (treeBinder.getDataPath() != null && finders != null)
		{
			FullTreeModelProxy lm = new FullTreeModelProxy(treeBinder, true); 

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
			dsImpl.setRootXPath("/");
	
			int[] path = getSelectedItem();
			if (path != null && path.length > 0)
			{
				selectedItemXPath = lm.getXPath(path);
				dsImpl.setXPath(selectedItemXPath == null ? treeBinder.getXPath(): selectedItemXPath);
				
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
	
	public void updateCurrentRow(JSONObject obj)
	{
        try {
    		StringWriter sb = new StringWriter();
    		JSONWriter writer = new JSONWriter(sb);
    		
    		if (selectedItem != null && selectedItem.length > 0) {
    			LinkedList<Integer> l = new LinkedList<Integer>();
    			for (int p: selectedItem) l.add(p);
    			dump (obj,  writer, -1, l);
    			
       			response("update_"+posToString(selectedItem), new AuInvoke(this, "updateRow", sb.toString()));
        	}
        } catch (Exception e) {
            throw new UiException(e);
        }
	}

	public void updateCurrentBranch(JSONObject obj)
	{
        try {
    		StringWriter sb = new StringWriter();
    		JSONWriter writer = new JSONWriter(sb);
    		
    		if (selectedItem != null && selectedItem.length > 0) {
    			LinkedList<Integer> l = new LinkedList<Integer>();
    			for (int p: selectedItem) l.add(p);
    			dump (obj,  writer, -1, l);
    			
       			response("update_"+posToString(selectedItem), new AuInvoke(this, "updateBranch", sb.toString()));
        	}
        } catch (Exception e) {
            throw new UiException(e);
        }
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
				if (isFinder(finder, type))
				{
					String icon = finder.optString("icon");
					if (icon != null && ! icon.isEmpty())
					{
						writer.key("icon");
						writer.value(getDesktop().getExecution().encodeURL( evaluateExpression(node, icon).toString().trim()));
					}
					Object cl = getRowClass(node);
					if (cl != null) {
						writer.key("$class");
						writer.value(cl);
					}
					String value = finder.optString("value");
					String template = finder.optString("template");
					JSONArray columnsArray = finder.optJSONArray("columns");
					if (columnsArray != null && !columnsArray.isEmpty()) {
						writer.key("columns");
						writer.array();
						writer.object();
						renderValue(node, writer, value, template);
						writer.endObject();
						for (int i = 0; i < columnsArray.length(); i++) {
							writer.object();
							JSONObject column = columnsArray.getJSONObject(i);
							String value2 = column.optString("value");
							String template2 = column.optString("template");
							renderValue(node, writer, value2, template2);
							writer.endObject();
						}
						writer.endArray();
					} else {
						renderValue(node, writer, value, template);
					}
					String leafExpr = finder.optString("leaf");
					if (leafExpr != null)
					{
						writer.key("leaf");
						if ("true".equals(leafExpr))
							writer.value(true);
						else if ("false".equals(leafExpr))
							writer.value(false);
						else {
							if (expf == null)
								expf = Expressions.newExpressionFactory(null);
							Object v = expf.evaluate(new JSONContext (this, node), translateHashExpression(leafExpr), Boolean.class);
							v = formatObject(v);
							writer.value(v != null && "true".equals(v.toString().trim()));
						}
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
					if (tail != null && !tail.trim().isEmpty()) {
						writer.key("tail");
						if (expf == null)
							expf = Expressions.newExpressionFactory(null);
						Object v = expf.evaluate(new JSONContext (this, node), translateHashExpression(tail), String.class);
						v = formatObject(v);
						writer.value(v);
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
	
	protected String getRowClass(JSONObject node) {
		return null;
	}
	
	public Object renderValue(JSONObject node, JSONWriter writer, String value, String template) {
		Object v = JSONObject.NULL;
		if (value != null && ! value.isEmpty())
		{
			v = evaluateExpression(node, "${"+value+"}");
			writer.key("value");
			writer.value(v);
		} else if (template != null)
		{
			v = evaluateExpression(node, template);
			writer.key("html");
			writer.value(v);
		}
		return v;
	}
	public Object evaluateExpression(JSONObject node, String expression) {
		Object v;
		if (expf == null)
			expf = Expressions.newExpressionFactory(null);
		v = expf.evaluate(new JSONContext (this, node), translateHashExpression(expression), String.class);
		v = formatObject(v);
		return v;
	}
	public String translateHashExpression(String template) {
		return template.replaceAll("\\#\\{", "\\$\\{");
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
	
	public void updateClient() {
		if (this.model != null) {
			TreeModelProxyNode root = (TreeModelProxyNode) model.getRoot();
			updateClient(root, new int[0]);
		}
	}

	private void updateClient(TreeModelProxyNode root, int [] pos) {
		int[] pos2 = Arrays.copyOf(pos, pos.length+1);
		if (root.areChildrenPopulated()) {
			TreeModelProxyNode[] children = root.getChildren();
			for (int i = 0; i < children.length; i++) 
			{
				pos2[pos.length] = i;
				onTreeDataContentChange(root, pos2);
				updateClient(children[i], pos2);
			}
		}
	}
	
	protected void dump(FullTreeModelProxy lm, TreeModelProxyNode node, JSONWriter writer, int levels, LinkedList<Integer> pos) {
		if (node == null ||node.getLocalPath() == null)
			return;
		writer.object();
		writer.key("position");
		writer.array();
		for (Integer p: pos) {
			writer.value(p);
		}
		writer.endArray();
		if (  ! node.getLocalPath().equals("."))
		{
			JSONObject finder = (JSONObject) node.getHint();
			String icon = finder.optString("icon");
			if (icon != null && ! icon.isEmpty())
			{
				writer.key("icon");
				writer.value(getDesktop().getExecution().encodeURL( (String) evaluateExpression(node, icon).toString().trim()));
			}
			Object cl = getRowClass(node);
			if (cl != null) {
				writer.key("class");
				writer.value(cl);
			}
			String value = finder.optString("value");
			String template = finder.optString("template");
			JSONArray columnsArray = finder.optJSONArray("columns");
			if (columnsArray != null && !columnsArray.isEmpty()) {
				writer.key("columns");
				writer.array();
				writer.object();
				renderValue(node, writer, value, template);
				writer.endObject();
				for (int i = 0; i < columnsArray.length(); i++) {
					writer.object();
					JSONObject column = columnsArray.getJSONObject(i);
					String value2 = column.optString("value");
					String template2 = column.optString("template");
					renderValue(node, writer, value2, template2);
					writer.endObject();
				}
				writer.endArray();
			} else {
				renderValue(node, writer, value, template);
			}
			String leafExpr = finder.optString("leaf");
			if (leafExpr != null)
			{
				writer.key("leaf");
				if ("true".equals(leafExpr))
					writer.value(true);
				else if ("false".equals(leafExpr))
					writer.value(false);
				else {
					if (expf == null)
						expf = Expressions.newExpressionFactory(null);
					Object v = expf.evaluate(new TreeNodeContext (this, node.getValue()), translateHashExpression(leafExpr), Boolean.class);
					v = formatObject(v);
					writer.value(v != null && "true".equals(v.toString().trim()));
				}
			}
			if (finder.optBoolean("collapsed"))
			{
				writer.key("collapsed");
				writer.value(true);
			}
			String tail = finder.optString("tail");
			if (tail != null) {
				writer.key("tail");
				if (expf == null)
					expf = Expressions.newExpressionFactory(null);
				Object v = expf.evaluate(new TreeNodeContext (this, node.getValue()), translateHashExpression(tail), String.class);
				v = formatObject(v);
				writer.value(v);
			}
		}
		
		if (levels > 0 || node.areChildrenPreloaded()) {
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
	
	protected String getRowClass(TreeModelProxyNode node) {
		return null;
	}
	
	protected boolean isFinder(JSONObject finder, String path) {
		String s1 = finder.optString("path");
		if (s1 == null) return false;
		String[] p1 = s1.split("/");
		String[] p2 = path.split("/");
		if (p2.length < p1.length) return false;
		for ( int i = 0; i < p1.length; i++) {
			String part1 = p1[p1.length - 1 - i];
			int j = part1.indexOf('[');
			if (j > 0) part1 = part1.substring(0, j);
			String part2 = p2[p2.length - 1 - i];
			j = part2.indexOf('[');
			if (j > 0) part2 = part2.substring(0, j);
			if ( !part2.equals(part1))
				return false;
		}
		return true;
	}

	public void renderValue(TreeModelProxyNode node, JSONWriter writer, String value, String template) {
		if (value != null && ! value.isEmpty())
		{
			JXPathContext ctx = treeBinder.getJXPathContext();
			Object v;
			try {
				v = ctx.getValue(node.getPointer().asPath()+"/"+value);
			} catch (JXPathNotFoundException e) {
				v = null;
			}
			v = formatObject(v);
			writer.key("value");
			writer.value(v);
		} else if (template != null)
		{
			Object v = evaluateExpression(node, template);
			writer.key("html");
			writer.value(v);
		}
	}
	public Object evaluateExpression(TreeModelProxyNode node, String expression) {
		if (expf == null)
			expf = Expressions.newExpressionFactory(null);
		Object value = node.getValue();
		if (value == null)
			return null;
		Object v = expf.evaluate(new TreeNodeContext (this, value), translateHashExpression(expression), String.class);
		v = formatObject(v);
		return v;
	}

	public Object formatObject(Object v) {
		if (v != null && v instanceof Calendar)
			v = DateFormats.getDateTimeFormat().format(((Calendar)v).getTime());
		else if (v != null && v instanceof Date)
			v = DateFormats.getDateTimeFormat().format((Date)v);
		return v;
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
            String s = sb.toString();
            if (!s.isEmpty())
            	response("insert_"+posToString(pos), new AuInvoke(this, "addBranch", s));
        } catch (Exception e) {
            throw new UiException(e);
        }
	}
	
	
	public void refreshCurrentRow()
	{
        try {
        	if (selectedItem != null && selectedItem.length > 0) {
        		TreeModelProxyNode node = model.getTreeModelProxyNode(selectedItem);
        		if (node != null)
        		{
        			StringWriter sb = new StringWriter();
        			LinkedList<Integer> posl = new LinkedList<Integer>();
        			for (int p: selectedItem) posl.add(p);
        			dump (model, node, new JSONWriter(sb), 0, posl);
        			response("update_"+posToString(selectedItem), new AuInvoke(this, "updateRow", sb.toString()));
        		}
        		
        	}
        } catch (Exception e) {
            throw new UiException(e);
        }
	}
	
	private void onTreeDataContentChange(TreeModelProxyNode parent, int pos[]) {
        try {
        	TreeModelProxyNode node = model.getTreeModelProxyNode(pos);
        	if (node != null)
        	{
	        	StringWriter sb = new StringWriter();
	        	LinkedList<Integer> posl = new LinkedList<Integer>();
	        	for (int p: pos) posl.add(p);
	        	dump (model, node, new JSONWriter(sb), 0, posl);
	            response("update_"+posToString(pos), new AuInvoke(this, "updateRow", sb.toString()));
        	}
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

	public void setSelectedIndexByXPath(String selectedXPath) {
		List<Integer> l = new LinkedList<Integer>();
		
		TreeModelProxyNode root = (TreeModelProxyNode) model.getRoot();
		String current = "";
		
		while (! current.equals(selectedXPath))
		{
			boolean any = false;
			TreeModelProxyNode[] children = root.getChildren();
			for (int i = 0; i < children.length; i++) {
				TreeModelProxyNode child = children[i];
				String path = child.getLocalPath();
				if (selectedXPath.startsWith(current+path)) {
					l.add(i);
					current = current+path;
					any = true;
					root = child;
					break;
				}
			}
			if (!any) return;
		}
		int r[] = new int[l.size()];
		for (int i = 0; i < l.size(); i++)
			r[i] = l.get(i).intValue();
		setSelectedIndex(r);
	}

    private void updateDataSource() {
    	if (model != null && dsImpl != null)
    	{
    		if (selectedItem == null || selectedItem.length == 0)
    		{
    			selectedItemXPath = null;
    			dsImpl.setXPath(treeBinder.getXPath());
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

    public String addNew (String path) {
    	return addNew (path, null);
    }
    
	public String addNew (String path, Object value)
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
			
			String nouXPath = null;
			if ( coll == null || ! (coll instanceof DataModelCollection)) {
				if (value == null)
					throw new UiException("Only a data model can be added without a value");
				else if (coll instanceof Collection) {
					Collection collection = (Collection) coll;
					collection.add(value);
					nouXPath = parentPath+"/"+path+"["+collection.size()+"]";
				} else {
					throw new UiException("Wrong xpath to add a value: "+path);
				}
			} else if (coll instanceof DataNodeCollection) {
				DataNodeCollection collection = (DataNodeCollection) coll;
				DataModelNode nouNode;
				if (value == null) 
					nouNode = collection.newInstance(); // Propaguem event onchange
				else {
					collection.add(value);
					nouNode = collection.getDataModel(collection.size()-1);
				}
				value = nouNode;
				nouXPath = nouNode.getXPath();
			} else {
				DataNodeCollection collection = (DataNodeCollection) coll;
				if (value == null) 
					throw new UiException("Only a data node can be added with a value");
				DataModelNode nouNode = collection.newInstance(); // Propaguem event onchange
				nouXPath = nouNode.getXPath();
				value = nouNode;
			}
			
			// Comprovem que item != null (nodes sense pare)
			int[] index = Arrays.copyOf(selectedItem, selectedItem.length + 1);
			TreeModelProxyNode[] children = node.getChildren();
			for (int i = 0; i < children.length; i++)
			{
				if (children[i].getValue() == value)
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
			
			if (table.model != null) {
				TreeModelProxyNode node = table.model.getTreeModelProxyNode(selectedItem);
				StringWriter sb = new StringWriter();
				JSONWriter writer = new JSONWriter(sb);
				table.dump (table.model, node, writer, 1, selectedItemList);
				table.response("setData", new AuInvoke(table, "addChildren", sb.toString()));
			} else {
				table.response("setData", new AuInvoke(table, "addChildren", "{}"));
			}
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
	public void download () {
		response("download", new AuInvoke(this, "downloadCsv", ""));
	}
}
