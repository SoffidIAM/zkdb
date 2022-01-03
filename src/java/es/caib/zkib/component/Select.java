package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;

import org.json.JSONObject;
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
import es.caib.zkib.binder.CollectionBinder;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.binder.list.ModelProxy;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.JXPContext;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.events.XPathValueEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.JXPathContextFactory;

public class Select extends XulElement implements XPathSubscriber, AfterCompose {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String dataPath;
	ListModel model;
    CollectionBinder collectionBinder = new CollectionBinder(this);
    protected Object selectedValue = "";
    boolean autocommit = false;
    boolean _updateValueBinder = true;
	private transient ListDataListener _dataListener;
	private static final String SELECT_EVENT = "onSelect"; //$NON-NLS-1$
	private static final String CLIENT_ACTION_EVENT = "onClientAction"; //$NON-NLS-1$
	boolean sort = false;
	String keyPath;
	String labelPath;
	EventListener onSelectListener = null;
	protected SingletonBinder valueBinder = new SingletonBinder(this);
	String options;
	boolean disabled = false;
	private int selectedPosition;
	
	public Select () {
		setSclass("select");
	}
	
    public String getDataPath() {
        return dataPath;
    }
    
    public void setDataPath(String bind) {
    	dataPath = bind;
        applyDataPath();
    }

    @Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Select self = this;
		final String uuid = self.getUuid();		

		wh.write("<select id=\"").write(uuid).write("\" z.type=\"zul.select.Select\"")
		.write(self.getOuterAttrs()).write(self.getInnerAttrs())
		.write(">")
		.write("</select>");
	}

	
	@Override
	public String getInnerAttrs() {
		StringBuffer sb = new StringBuffer ( super.getInnerAttrs() );
		
		if (options != null)
			HTMLs.appendAttribute(sb, "options", options);
		else if (getModel() != null)
			HTMLs.appendAttribute(sb, "options", generateValuesList(getModel()).toString());
		if (selectedValue != null)
			HTMLs.appendAttribute(sb, "value", getSelectedPosition());
		HTMLs.appendAttribute(sb, "sort", sort);
		if (disabled)
			HTMLs.appendAttribute(sb, "disabled", disabled);

		return sb.toString();

	}


    private String getSelectedPosition() {
    	if ( model != null) {
    		for (int i =  0; i < model.getSize(); i++) {
    			Object o = model.getElementAt(i);
    			if (o != null)
    			{
    				Object key = JXPathContextFactory.newInstance().newContext(null, o).getValue(keyPath);
    				if (key != null && key.equals(selectedValue)) {
    			   		return Integer.toString(i);
    				}
    			}
    		}

    	}
    	return selectedValue.toString();
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

				StringBuffer sb = generateValuesList(model);
				smartUpdate("options", sb.toString());
				initDataListener();
			}

		} else if (this.model != null) {
			this.model.removeListDataListener(_dataListener);
			this.model = null;
			smartUpdate("options", "[]");
		}
	}
	

	public StringBuffer generateValuesList(ListModel model) {
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < model.getSize();  i++)
		{
			String s = getClientValue(i);
			if ( sb.length() > 1)
				sb.append(",");
			sb.append(s);
		}
		sb.append("]");
		return sb;
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
		            response("new_"+i, new AuInvoke(this, "deleteRow", Integer.toString(i)));
		        } catch (Exception e) {
		            throw new UiException(e);
		        }
			}
		}
        if (event.getType() == XPathCollectionEvent.UPDATED) {
        	ModelProxy dm = (ModelProxy) getModel();
        	for (int i = event.getIndex0(); i <= event.getIndex1(); i++)
        	{
	            String value = getClientValue(i);
	            response("update_"+i, new AuInvoke(this, "updateRow", Integer.toString(i), value));
        	}
        }
		
	}
	
	private void enableOnSelectListener() {
		if (onSelectListener == null) {
            onSelectListener = new OnSelectListener();

            this.addEventListener("onSelect", onSelectListener);

        }
	}

    public String getXPath() {
        return collectionBinder.getModelXPath();
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
        } catch (Exception e) {
            throw new UiException(e);
        }
    }

    private String getClientValue(int pos) {
    	if (getModel() == null)
    		return "{}";
    	Object element = getModel().getElementAt(pos);
    	JXPathContext ctx = JXPathContextFactory.newInstance().newContext(null, element);
    	Object value = pos;
    	Object label = labelPath == null ? value :  ctx.getValue(labelPath);
    	JSONObject o = new JSONObject();
    	o.put("value", value);
    	o.put("label", label == null? "": label);
    	return o.toString();
	}

    public void setPage(Page page) {
        super.setPage(page);
        collectionBinder.setPage(page);
    }

    public void setParent(Component parent) {
        super.setParent(parent);
        collectionBinder.setParent(parent);
        valueBinder.setParent(parent);
        syncSelectedItem();
    }

    public Object clone() {
        Select clone = (Select) super.clone();
        clone.valueBinder = new SingletonBinder(clone);
        clone.collectionBinder = new CollectionBinder(clone);
        clone.setDataPath(collectionBinder.getDataPath());
        clone.setBind(valueBinder.getDataPath());
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

    /**
     * 
     */
    private void syncSelectedItem() {
    	if (valueBinder.getDataPath() != null && valueBinder.isValid() &&  !valueBinder.isVoid()) {
    		Object value = valueBinder.getValue();
    		selectedValue = value;
    		if (model == null) {
    			smartUpdate("selected", selectedValue == null ? null:
    				selectedValue.toString());
    		} else {
    			
    			for (int i =  0; i < model.getSize(); i++) {
    				Object o = model.getElementAt(i);
    				if (o != null)
    				{
    					Object key = JXPathContextFactory.newInstance().newContext(null, o).getValue(keyPath);
    					if (key == null ? value == null : key.equals(value)) {
    						smartUpdate("selected", Integer.toString(i));
    						return;
    					}
    				}
    			}
   				Object o = model.getElementAt(0);
				if (o != null)
				{
					Object key = JXPathContextFactory.newInstance().newContext(null, o).getValue(keyPath);
					valueBinder.setValue(key);
				}
    			smartUpdate("selected", "0");
    		}
    		
    	}
    }

	public void afterCompose() {
		syncSelectedItem();
	}

	public void onUpdate(XPathEvent event) {
        if (event instanceof XPathValueEvent) {
            syncSelectedItem();
        }
        if (event instanceof XPathCollectionEvent) {
            XPathCollectionEvent e = (XPathCollectionEvent) event;
            if (e.getType() == XPathCollectionEvent.FOCUSNODE
                    && getModel() instanceof ModelProxy) {
                ModelProxy mp = (ModelProxy) getModel();
                int i = mp.getPosition(e.getIndex());
                if (i >= 0) {
                    setSelectedValue(null);
                }
            }
            if (e.getType() == XPathCollectionEvent.RECREATED
                    && getModel() instanceof ModelProxy) {
                setSelectedValue(null);
            }
        }
        if (event instanceof XPathRerunEvent) {
            boolean oldValue = _updateValueBinder;
            _updateValueBinder = false;
            applyDataPath();
            setSelectedValue(null);
            valueBinder.setDataPath(valueBinder.getDataPath());
            syncSelectedItem();
            _updateValueBinder = oldValue;
        }

	}

	public Command getCommand(String cmdId) {
		if (SELECT_EVENT.equals(cmdId))
			return _onSelectCommand;
		if (CLIENT_ACTION_EVENT.equals(cmdId))
			return _onClientActionCommand;
		
		return super.getCommand(cmdId);
	}

	private static Command _onSelectCommand  = new ComponentCommand (SELECT_EVENT, 0) {
		protected void process(AuRequest request) {
			final Select table = (Select) request.getComponent();
			table.clientSelect (request.getData()[0]);
		}
	};

	private static Command _onClientActionCommand  = new ComponentCommand (CLIENT_ACTION_EVENT, 0) {
		protected void process(AuRequest request) {
			final Select table = (Select) request.getComponent();
			String[] data = request.getData();
			String event = data[0];
			Object[] newData = new Object [data.length-1];
			for (int i = 1; i < newData.length; i++)
				newData[i] = data[i+1];
			Events.postEvent(new Event(event, table, newData));
		}
		
	};

    public void setBind(String bind) {
        valueBinder.setDataPath(bind);
        if (bind != null)
        	enableOnSelectListener();
    }

    protected void clientSelect(String value ) {
    	if (model != null) {
    		int pos = Integer.parseInt(value);
    		Object data = model.getElementAt(pos);
    		selectedValue = JXPathContextFactory.newInstance().newContext(null, data).getValue(keyPath);
    	} else {
    		selectedValue = value;
    	}
		Events.postEvent(new Event("onSelect", this, selectedValue));
		valueBinder.setValue(selectedValue);
	}

	public String getBind() {
    	return valueBinder.getDataPath();
    }

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		try {
			this.options = new Yaml2Json().transform(options);
		} catch (IOException e) {
			throw new UiException("Unable to parse JSON descriptor "+options);
		}

		smartUpdate("options", this.options);
	}

	public Object getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(String selectedValue) {
		smartUpdate("selected", selectedValue);
		this.selectedValue = selectedValue;
	}

	public boolean isSort() {
		return sort;
	}

	public void setSort(boolean sort) {
		this.sort = sort;
	}

	public String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

	public String getLabelPath() {
		return labelPath;
	}

	public void setLabelPath(String labelPath) {
		this.labelPath = labelPath;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		smartUpdate("disabled", disabled);
	}

}
