package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.util.TimeZones;
import org.zkoss.util.resource.Labels;
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
import org.zkoss.zul.ListModel;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;
import org.zkoss.zul.impl.XulElement;

import es.caib.zkdb.yaml.Yaml2Json;
import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datasource.ChildDataSourceImpl;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Variables;

public class DataTable extends XulElement implements XPathSubscriber,
	BindContext, DataSource, AfterCompose {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String columns = null;
	private String dataPath;
	ListModel model;
    CollectionBinder collectionBinder = new CollectionBinder(this);
    ChildDataSourceImpl dsImpl = null;
    int selectedIndex = -1;
    List<Integer> selectedIndexList = new LinkedList<Integer>();
	private String selectedItemXPath;
    boolean autocommit = false;
    boolean _updateValueBinder = true;
	private transient ListDataListener _dataListener;
	private static final String SELECT_EVENT = "onSelect"; //$NON-NLS-1$
	private static final String MULTI_SELECT_EVENT = "onMultiSelect"; //$NON-NLS-1$
	private static final String CLIENT_ACTION_EVENT = "onClientAction"; //$NON-NLS-1$
	private static final String REORDER_EVENT = "onReorder"; //$NON-NLS-1$
	boolean enablefilter = true;
	int sortColumn = -1;
	int sortDirection = +1;
	boolean footer = true;
	boolean multiselect = false;
	String maxheight = "";
	String rowsMsg = "Rows";
	String downloadMsg = "Download";
	boolean download = false;
	boolean updateRowEvent = true;
	boolean reorder = false;
	boolean translatelabels = false;
	private boolean initialized = false;
	private JSONArray columnsArray;

	public DataTable () {
		setSclass("datatable");
		String r = Labels.getLabel("zkdb.rows");
		if (r != null) rowsMsg = r;
		
		String d = Labels.getLabel("zkdb.download");
		if (d != null) downloadMsg = d;
	}
    public String getDataPath() {
        return dataPath;
    }
    
    public String getSelectedItemXPath () {
    	return selectedItemXPath;
    }
    
    public String getItemXPath(int position) {
    	String base = collectionBinder.getModelXPath();
        String p = ((ModelProxy) getModel()).getBind(position);
        return XPathUtils.concat(base, p);
    }

    public void setDataPath(String bind) {
    	dataPath = bind;
        applyDataPath();
    }

    @Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final DataTable self = this;
		final String uuid = self.getUuid();		

		wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.datatable.Datatable\"")
		.write(self.getOuterAttrs()).write(self.getInnerAttrs())
		.write(">")
		.write("</div>");
		
		if (getModel() != null && columns != null)
			sendModelData();
	}

	
	@Override
	public String getInnerAttrs() {
		StringBuffer sb = new StringBuffer ( super.getInnerAttrs() );
		
		HTMLs.appendAttribute(sb, "columns", columns);
		HTMLs.appendAttribute(sb, "enablefilter", enablefilter);
		HTMLs.appendAttribute(sb, "sortDirection", sortDirection);
		HTMLs.appendAttribute(sb, "sortColumn", sortColumn);
		HTMLs.appendAttribute(sb, "multiselect", multiselect);
		HTMLs.appendAttribute(sb, "footer", footer);
		HTMLs.appendAttribute(sb, "maxheight", maxheight);
		HTMLs.appendAttribute(sb, "msgrows", rowsMsg);
		HTMLs.appendAttribute(sb, "reorder", reorder);
		if (download)
			HTMLs.appendAttribute(sb, "msgdownload", downloadMsg);

		return sb.toString();

	}


	public String getColumns() {
		return columns;
	}


	public void setColumns(String columns) {
		try {
			this.columns = new Yaml2Json().transform(columns);
			columnsArray = new JSONArray(this.columns);
		} catch (IOException e) {
			throw new UiException("Unable to parse JSON descriptor "+columns);
		}
		
		smartUpdate("columns", this.columns);
		
		if (model != null)
			sendModelData();
	}

    /**
     * 
     */
    private void applyDataPath() {
        ListModel oldListModel = getModel();
        if (oldListModel != null && oldListModel instanceof ModelProxy)
            ((ModelProxy) oldListModel).unsubscribe();

       	collectionBinder.setDataPath(dataPath);

       	ListModel lm = collectionBinder.createModel();
        setModel(lm);

        updateDataSource();
	
    }

	public void updateDataSource() {
		ListModel lm = getModel();
		if (lm == null) {
            if (dsImpl != null)
                dsImpl.setRootXPath(null);
        } else if (getPage () != null) {
            if (dsImpl == null)
                dsImpl = new ChildDataSourceImpl();

            dsImpl.setDataSource(collectionBinder.getDataSource());
            if (lm instanceof ModelProxy) {
                selectedItemXPath = ((ModelProxy) getModel())
                        .getBind(getSelectedIndex());
                dsImpl.setRootXPath(collectionBinder.getModelXPath(), selectedItemXPath);
            } else {
                dsImpl.setRootXPath(collectionBinder.getModelXPath());
            }
        }
	}

	public ListModel getModel() {
		return model;
	}

	public void setModel(ListModel model) {
		if (model != null) {
			if (this.model != model) {
				if (this.model != null) {
					this.model.removeListDataListener(_dataListener);
				}
				this.model = model;

				if (columns != null)
					sendModelData();
				initDataListener();
			}

		} else if (this.model != null) {
			this.model.removeListDataListener(_dataListener);
			this.model = null;
			response("setData", new AuInvoke(this, "setData", "[]"));
		}
	}
	
	public void sendModelData() {
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < model.getSize();  i++)
		{
			String s = getClientValue(i);
			if ( sb.length() > 1)
				sb.append(",");
			sb.append(s);
		}
		sb.append("]");
		response("setData", new AuInvoke(this, "setData", sb.toString()));
	}

	/** Initializes _dataListener and register the listener to the model
	 */
	private void initDataListener() {
		if (_dataListener == null)
			_dataListener = new ListDataListener() {
				public void onChange(ListDataEvent event) {
					onListDataChange(event);
				}

			};

		model.addListDataListener(_dataListener);
	}

	private void onListDataChange(ListDataEvent event) {
		if (event.getType() == ListDataEvent.INTERVAL_ADDED)
		{
			ModelProxy dm = (ModelProxy) getModel();
			StringBuffer sb = new StringBuffer("[");
			for (int i = event.getIndex0(); i <= event.getIndex1(); i++) 
			{
		        try {
		            String value = getClientValue(i);
		            if (sb.length() > 1) sb.append(",");
		            sb.append(value);
		        } catch (Exception e) {
		            throw new UiException(e);
		        }
			}
			sb.append("]");
			response("new_"+event.getIndex0(), new AuInvoke(this, "addRows", Integer.toString(event.getIndex0()), sb.toString()));
		}
		if (event.getType() == ListDataEvent.INTERVAL_REMOVED)
		{
			ModelProxy dm = (ModelProxy) getModel();
			for (int i = event.getIndex0(); i <= event.getIndex1(); i++) 
			{
		        try {
		            response("remove_"+i, new AuInvoke(this, "deleteRow", Integer.toString(i)));
		        } catch (Exception e) {
		            throw new UiException(e);
		        }
			}
		}
        if (event.getType() == XPathCollectionEvent.UPDATED && updateRowEvent) {
        	ModelProxy dm = (ModelProxy) getModel();
        	for (int i = event.getIndex0(); i <= event.getIndex1(); i++)
        	{
	            String value = getClientValue(i);
	            response("update_"+i, new AuInvoke(this, "updateRow", Integer.toString(i), value));
        	}
        }
		
	}

	public void updateClientRow(int i) {
		if (updateRowEvent) {
            String value = getClientValue(i);
            response("update_"+i, new AuInvoke(this, "updateRow", Integer.toString(i), value));
		}
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public int[] getSelectedIndexes() {
		int[] r = new int[selectedIndexList.size()];
		for (int i = 0; i < selectedIndexList.size(); i++)
			r[i] = selectedIndexList.get(i).intValue();
		return r;
	}

	public void setSelectedIndex(int selectedIndex) {
		if (this.getSelectedIndex() != selectedIndex)
		{
			response("setSelected", new AuInvoke(this, "setSelected", Integer.toString(selectedIndex)));
		}
		this.selectedIndex = selectedIndex;
		selectedIndexList.clear();
		if (selectedIndex >= 0)
		{
			selectedIndexList.add(selectedIndex);
		}
		updateDataSource();
	}

	public void setSelectedIndex(int[] selectedIndex) {
		if ( !multiselect)
		{
			throw new UiException("Only multiselect table can select more than one item");
		}
		selectedIndexList.clear();
		JSONArray array = new JSONArray();
		for (int i: selectedIndex)
		{
			array.put(i);
			selectedIndexList.add(i);
		}

		response("setSelected", new AuInvoke(this, "setSelectedMulti", array.toString()));

		updateDataSource();
	}

	public JXPathContext getJXPathContext() {
		if (dsImpl == null)
			updateDataSource();
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
            int i = getSelectedIndex();

            if (getModel() != null && getModel() instanceof ModelProxy
                    && dsImpl != null) {
                selectedItemXPath = ((ModelProxy) getModel()).getBind(i);
                dsImpl.setXPath(selectedItemXPath);
            }

        }
        super.smartUpdate(attr, value);
    }

    public String getXPath() {
        return collectionBinder.getModelXPath();
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
            if (collectionBinder != null
                    && collectionBinder.getDataSource() != null)
                collectionBinder.getDataSource().commit();
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

    public void addNew() {
        if (!(getModel() instanceof ModelProxy))
            throw new UiException(
                    "Not allowed to creaete records without a model");

        if (autocommit)
            commit();

        try {
        	ModelProxy dm = (ModelProxy) getModel();
        	int i = dm.newInstance();
            String value = getClientValue(i);
            response("new_"+i, new AuInvoke(this, "addRow", Integer.toString(i), value));
            setSelectedIndex(i);
        } catch (Exception e) {
            throw new UiException(e);
        }
    }

    protected String getClientValue(int pos) {
    	if (getModel() == null)
    		return "{}";
    	Object element = getModel().getElementAt(pos);
    	if (element == null)
    		return "{}";
    	JSONObject o = getClientValue(element);
    	
    	return o.toString();
	}
    
	protected JSONObject getClientValue(Object element) throws JSONException {
		JSONObject o = wrap(element);
		return o;
	}

	public JSONObject wrap(Object element) {
		JSONObject o;
		if (element == null)
			o = new JSONObject();
		if (element instanceof ClassLoader)
			return null;
		if (element instanceof DynaBean) {
			DynaBean dynaBean = (DynaBean) element;
			DynaClass cl = dynaBean.getDynaClass();
			o  = new JSONObject();
			for ( DynaProperty p: cl.getDynaProperties()) {
				String name = p.getName();
				if (!name.equals("class") && 
					!name.equals("instance") &&
					! DataModelCollection.class.isAssignableFrom( p.getType()) ) 
				{
					Object value = dynaBean.get(name);
					wrapClientValue(o, name, value);
				}
			}
		} else {
			if (element instanceof DataNode) element = ((DataNode) element).getInstance();
			Map<String, Object> p;
			try {
				p = PropertyUtils.describe(element);
			} catch (Exception e) {
				throw new JSONException(e);
			}
			o = new JSONObject();
			for (Entry<String, Object> entry: p.entrySet()) {
				wrapClientValue(o, entry.getKey(), entry.getValue());
			}
		}
		return o;
	}
	
	public void wrapClientValue(JSONObject o, String name, Object value) {
		if (value instanceof ClassLoader) {
			// Nothing to do
		} else if (value instanceof Date) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
			dateFormat.setTimeZone(TimeZones.getCurrent());
			String v =  dateFormat.format((Date)value);
			o.put(name, JSONObject.wrap(v));
			o.put(name+"_date", DateFormats.getDateFormat().format(value));
			o.put(name+"_datetime", DateFormats.getDateTimeFormat().format(value));
		} else if (value instanceof Calendar) {
			Date d = ((Calendar) value).getTime();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss");
			dateFormat.setTimeZone(TimeZones.getCurrent());
			String v =  dateFormat.format(d);
			o.put(name, JSONObject.wrap(v));
			o.put(name+"_date", DateFormats.getDateFormat().format(d));
			o.put(name+"_datetime", DateFormats.getDateTimeFormat().format(d));
		} else if (value instanceof byte[]) {
			byte[] img = (byte[]) value;
			o.put(name, Base64.getEncoder().encodeToString(img));
			String ct = img[0] == '<' && img[1] == '?' ? "image/svg+xml" :
				img[0] == 0x77 && img[1] == 0xd8 && img[2] == 0xff ?  "image/jpeg":
				img[0] == 0x89 && img[1] == 0x50 && img[2] == 0x4e ?  "image/png":
					"image/gif";
			o.put(name+"_contentType", ct);

		} else if (value instanceof Map) {
			Map m = (Map) value;
			JSONObject o2 = new JSONObject();
			o.put(name, o2);
			for (Object name2: m.keySet()) {
				wrapClientValue(o2, name2.toString(), m.get(name2));
			}
		} else {
			if (translatelabels && value != null) {
				try {
					Method m = value.getClass().getMethod("getValue");
					if (m != null) {
						String className = value.getClass().getName();
						value = m.invoke(value);
						value = Labels.getLabel(className+"."+value);
					}
				} catch (Exception e) {
					// Ignore reflection errors
				}
			}
			o.put(name, JSONObject.wrap(value));
		}
	}
    

	public void delete() {
        ListModel lm = getModel();
        if (lm == null)
        {
        	if (selectedIndex >= 0) {
		        try {
		            response("remove_"+selectedIndex, new AuInvoke(this, "deleteRow", Integer.toString(selectedIndex)));
		        } catch (Exception e) {
		            throw new UiException(e);
		        }
		        selectedIndex = -1;
        	}
        } else {
        	if (!(lm instanceof ModelProxy))
        		throw new UiException(
        				"Not allowed to delete records without a model");
        	
        	if (getSelectedIndex() >= 0 || multiselect && !selectedIndexList.isEmpty()) {
        		int[] s = getSelectedIndexes();
        		Arrays.sort(s);
        		for (int i = s.length - 1; i >= 0; i--) {
        			int pos = s[i];
   					Object obj = lm.getElementAt(pos);
        			if (obj instanceof DataModelNode) {
        				((DataModelNode) obj).delete();
        			} else {
        				DataSource dataSource = collectionBinder.getDataSource();
        				JXPathContext ctx = dataSource.getJXPathContext();
        				Object c = ctx.getValue(collectionBinder.getXPath());
        				if (c instanceof Collection) {
        					((Collection) c).remove(obj);
        					ctx.setValue(collectionBinder.getXPath(), c);
        					dataSource.sendEvent(new XPathRerunEvent(dataSource, collectionBinder.getXPath()));
        				} else {
        					throw new UiException( "Unable to delete object "+obj.toString());
        				}
        			}
        		}
        		
        		// response("remove_"+getSelectedIndex(), new AuInvoke(this, "removeRow", Integer.toString(getSelectedIndex())));
        		if (autocommit)
        			commit();
        		setSelectedIndex(-1);
        		if (multiselect)
        			setSelectedIndex(new int[0]);
        	}
        	
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
        selectedIndex = -1;
        selectedIndexList.clear();
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
        syncSelectedItem();
    }

    public Object clone() {
        DataTable clone = (DataTable) super.clone();
        clone._dataListener = null;
        clone.collectionBinder = new CollectionBinder(clone);
        clone.setDataPath(collectionBinder.getDataPath());
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
	

    /**
     * 
     */
    private void syncSelectedItem() {
    }

	public void afterCompose() {
	}

    public DataSource getDataSource() {
        return collectionBinder.getDataSource();
    }

    public void sendEvent(XPathEvent event) {
        dsImpl.sendEvent(event);
    }

	public void onUpdate(XPathEvent event) {
        if (event instanceof XPathCollectionEvent) {
            XPathCollectionEvent e = (XPathCollectionEvent) event;
            if (e.getType() == XPathCollectionEvent.FOCUSNODE
                    && getModel() instanceof ModelProxy) {
                ModelProxy mp = (ModelProxy) getModel();
                int i = mp.getPosition(e.getIndex());
                if (i >= 0) {
                    setSelectedIndex(i);
                }
            }
            if (e.getType() == XPathCollectionEvent.RECREATED
                    && getModel() instanceof ModelProxy) {
                setSelectedIndex(-1);
            }
        }
        if (event instanceof XPathRerunEvent) {
            applyDataPath();
       		setSelectedIndex(-1);
            syncSelectedItem();
        }
	}

	public Command getCommand(String cmdId) {
		if (SELECT_EVENT.equals(cmdId))
			return _onSelectCommand;
		if (MULTI_SELECT_EVENT.equals(cmdId))
			return _onMultiSelectCommand;
		if (CLIENT_ACTION_EVENT.equals(cmdId))
			return _onClientActionCommand;
		if (REORDER_EVENT.equals(cmdId))
			return _onDragCommand;
			
		return super.getCommand(cmdId);
	}

	private static Command _onDragCommand  = new ComponentCommand (REORDER_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTable table = (DataTable) request.getComponent();
			ReorderEvent ev  = new ReorderEvent("onReorder", table);
			ev.setSrcPosition( Integer.parseInt( request.getData()[0] ) );
			int insertBefore = Integer.parseInt( request.getData()[1]) ;
			if (insertBefore >= 0)
				ev.setInsertBeforePosition( insertBefore );
			if (table.getModel() != null) {
				ev.setSrcObject( table.getModel().getElementAt(ev.getSrcPosition()) );
				if ( insertBefore >= 0)
					ev.setInsertBeforeObject( table.getModel().getElementAt( insertBefore ) );
			}
			Events.postEvent(ev);
		}
	};

	private static Command _onSelectCommand  = new ComponentCommand (SELECT_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTable table = (DataTable) request.getComponent();
			table.selectedIndex = Integer.parseInt( request.getData()[0] );
			table.selectedIndexList.clear();
			table.selectedIndexList.add(table.selectedIndex);
			table.updateDataSource();
			Events.postEvent(new Event("onSelect", table, new Integer(table.selectedIndex)));
		}
		
	};

	private static Command _onMultiSelectCommand  = new ComponentCommand (MULTI_SELECT_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTable table = (DataTable) request.getComponent();
			JSONArray array = new JSONArray(request.getData()[0]);
			table.selectedIndexList.clear();
			for (int i = 0; i < array.length(); i++)
			{
				table.selectedIndexList.add(array.getInt(i));
			}
			if (table.selectedIndexList.size() == 1)
				table.selectedIndex = table.selectedIndexList.get(0);
			else
				table.selectedIndex = -1;
			table.updateDataSource();
			Events.postEvent(new Event("onMultiSelect", table, new Integer(table.selectedIndex)));
		}
		
	};

	private static Command _onClientActionCommand  = new ComponentCommand (CLIENT_ACTION_EVENT, 0) {
		protected void process(AuRequest request) {
			final DataTable table = (DataTable) request.getComponent();
			String[] data = request.getData();
			String event = data[0];
			String[] newData = new String [data.length-1];
			for (int i = 0; i < newData.length; i++)
				newData[i] = data[i+1];
			Events.postEvent(new Event(event, table, newData));
		}
		
	};

	public void deleteSelectedItem() {
		delete();
	}
	public boolean isEnablefilter() {
		return enablefilter;
	}
	public void setEnablefilter(boolean enablefilter) {
		this.enablefilter = enablefilter;
		if (! enablefilter) {
			setSclass(getSclass()+" fixedlayout");
		}
	}
	public int getSortColumn() {
		return sortColumn;
	}
	public void setSortColumn(int sortColumn) {
		this.sortColumn = sortColumn;
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
	public boolean isMultiselect() {
		return multiselect;
	}
	public void setMultiselect(boolean multiselect) {
		this.multiselect = multiselect;
		smartUpdate("multiselect", Boolean.toString(multiselect));
	}
	public String getMaxheight() {
		return maxheight;
	}
	public void setMaxheight(String maxheight) {
		this.maxheight = maxheight;
		smartUpdate("maxheight", maxheight);
	}
	public String getRowsMsg() {
		return rowsMsg;
	}
	public void setRowsMsg(String rowsMsg) {
		this.rowsMsg = rowsMsg;
	}
	public String getDownloadMsg() {
		return downloadMsg;
	}
	public void setDownloadMsg(String downloadMsg) {
		this.downloadMsg = downloadMsg;
	}
	
	public void setData (String data) {
		response("setData", new AuInvoke(this, "setData", data));
	}

	public void setData (JSONArray data) {
		response("setData", new AuInvoke(this, "setData", data.toString()));
	}
	
	public void download () {
		response("download", new AuInvoke(this, "downloadCsv", ""));
	}
	public boolean isDownload() {
		return download;
	}
	public void setDownload(boolean download) {
		this.download = download;
	}
	public boolean isUpdateRowEvent() {
		return updateRowEvent;
	}
	public void setUpdateRowEvent(boolean updateRowEvent) {
		this.updateRowEvent = updateRowEvent;
	}
	public boolean isReorder() {
		return reorder;
	}
	public void setReorder(boolean reorder) {
		this.reorder = reorder;
		smartUpdate("reorder", reorder);
	}
	public boolean isTranslatelabels() {
		return translatelabels;
	}
	public void setTranslatelabels(boolean translatelabels) {
		this.translatelabels = translatelabels;
	}
	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
        collectionBinder.setPage(newpage);
	}
	@Override
	public void onPageDetached(Page page) {
		super.onPageDetached(page);
		collectionBinder.setPage(null);
		setModel(null);
	}

	public void refresh () throws Exception {
		if (collectionBinder != null && collectionBinder.isValid())
			collectionBinder.getUpdateableListModel().refresh();
	}
}