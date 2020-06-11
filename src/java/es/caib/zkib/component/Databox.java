package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONStringer;
import org.zkoss.mesg.Messages;
import org.zkoss.util.resource.Labels;
import org.zkoss.xml.HTMLs;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.mesg.MZul;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.binder.SingletonBinder;
import es.caib.zkib.datasource.DataSource;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class Databox extends InputElement implements XPathSubscriber, AfterCompose {
	private static final long serialVersionUID = -6447081728620764693L;
	private SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate = false;
	private String placeholder;
	private List<String> values;
	private List<String> descriptions;
	private List collectionValue;
	boolean multiValue;
	boolean required;
	boolean readOnly;
	boolean disabled;
	Integer maxlength;
	boolean composed = false;
	enum Type {
		STRING,
		NAME_DESCRIPTION,
		DESCRIPTION,
		DATE,
		BOOLEAN,
		LIST,
		IMAGE,
		HTML
	};

	String label;
	Object value;
	
	Type type = Type.STRING;
	String icon;
	String removeIcon = "~./img/remove.png";
	String calendarIcon = "~./zul/img/caldrbtn.gif";
	String selectIcon = "~./img/magnifier.png";
	String waitIcon = "/img/wait.gif"; 
	String warningIcon = "~./img/warning.gif";
	String format;

	public Databox() {
		setSclass("databox");
	}
	
	public void undo(){
		binder.setOldValue();
	}
	
	/* (non-Javadoc)
	 * @see com.centillex.zk.Bindable#getBind()
	 */
	public String getBind() {		
		return binder.getDataPath();
		
	}

	public void setValue(Object value) throws WrongValueException {
		if (! duringOnUpdate )
		{
			binder.setValue(value);
		}
		this.value = value;
		refreshValue();
	}

	public void setValue(Integer position, Object value) throws WrongValueException {
		if (position == null)
			setValue(value);
		else {
			onItemChange(value, position);
			response("set_value_"+position,
					new AuInvoke(this, "setValue", position.toString(), 
							normalize(value).toString()));
		}
	}

	@Override
	protected void validate(Object value) throws WrongValueException {
		if (! duringOnUpdate)
			super.validate(value);
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#getDataSource()
	 */
	public DataSource getDataSource() {
		return binder.getDataSource();
	}

	/* (non-Javadoc)
	 * @see es.caib.seycon.net.web.zul.BinderImplementation#setBind(java.lang.String)
	 */
	public void setBind(String bind) {
		binder.setDataPath(bind);
		if (bind != null)
		{
			refreshValue ();
			DataSource ds = getTopDatasource();
			if (ds instanceof Component)
				smartUpdate("dsid", ((Component) ds).getUuid());
			else
				smartUpdate("dsid", null);
		}
		else
			smartUpdate("dsid", null);
	}

	public DataSource getTopDatasource() {
		DataSource ds = binder.getDataSource();
		if (ds != null )
			do {
				if (ds instanceof BindContext)
					ds = ((BindContext)ds).getDataSource();
				else
					break;
			} while (true);
		return ds;
	}

	public void onUpdate (XPathEvent event) {
		refreshValue ();
	}

	private void refreshValue ()
	{
		if ( !composed )
			return;
		
		if (binder.getDataPath() != null) {
			Object newVal = binder.getValue();
			if (newVal == null)
				newVal = multiValue ? new LinkedList<Object>(): "";
				
			this.value = newVal;
				
			if (!binder.isVoid () && ! binder.isValid())
				super.setDisabled (true);
			else
				super.setDisabled(effectiveDisabled);
		}		

		String stringValue = wrapValue();

		smartUpdate("value", stringValue);
	}
	
	public boolean effectiveDisabled = false;
	private String currentSearch;
	/* (non-Javadoc)
	 * @see org.zkoss.zul.impl.InputElement#setDisabled(boolean)
	 */
	public void setDisabled(boolean disabled) {
		effectiveDisabled = disabled;
		super.setDisabled(disabled);
	}

	public void setPage(Page page) {
		super.setPage(page);
		binder.setPage(page);
	}
	
	public void setOldValue() {
		binder.setOldValue();
	}

	public void setParent(Component parent) {
		super.setParent(parent);
		binder.setParent(parent);
	}

	public Object clone() {
		Databox clone = (Databox) super.clone();
		clone.binder = new SingletonBinder (clone);
		clone.binder.setDataPath(binder.getDataPath());
		clone.effectiveDisabled = effectiveDisabled;
		return clone;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getInnerAttrs() {
		final StringBuffer sb =
			new StringBuffer(64).append(super.getInnerAttrs());
		HTMLs.appendAttribute(sb, "placeholder", placeholder);
		if (binder.getDataPath() != null)
		{
			DataSource ds = getTopDatasource();
			if (ds != null && ds instanceof Component)
				HTMLs.appendAttribute(sb, "dsid", ((Component)ds).getUuid());
				
		}
		
		if (value != null) {
			String stringValue;
			stringValue = wrapValue();
			HTMLs.appendAttribute(sb, "value", stringValue);
		}
		HTMLs.appendAttribute(sb, "label", label);		
		HTMLs.appendAttribute(sb, "disabled", disabled);
		HTMLs.appendAttribute(sb, "readonly", readOnly);
		HTMLs.appendAttribute(sb, "multivalue", multiValue);
		HTMLs.appendAttribute(sb, "required", required);
		if (maxlength != null)
			HTMLs.appendAttribute(sb, "maxlength", maxlength);
		HTMLs.appendAttribute(sb, "removeicon", getDesktop().getExecution().encodeURL(removeIcon));
		if (icon != null)
			HTMLs.appendAttribute(sb, "icon", getDesktop().getExecution().encodeURL(icon));
		if (warningIcon != null)
			HTMLs.appendAttribute(sb, "warningicon", getDesktop().getExecution().encodeURL(warningIcon));
		if (waitIcon != null)
			HTMLs.appendAttribute(sb, "waiticon", getDesktop().getExecution().encodeURL(waitIcon));
		if (type == Type.DATE && calendarIcon != null)
			HTMLs.appendAttribute(sb, "calendaricon", getDesktop().getExecution().encodeURL(calendarIcon));
			
		if ((type == Type.NAME_DESCRIPTION || type == Type.DESCRIPTION) && selectIcon != null)
			HTMLs.appendAttribute(sb, "selecticon", getDesktop().getExecution().encodeURL(selectIcon));

		if (values != null && type == Type.LIST)
		{
			HTMLs.appendAttribute(sb, "values", new JSONArray(values).toString());
		}
		
		if (descriptions != null) {
			HTMLs.appendAttribute(sb, "description", new JSONArray(descriptions).toString());
		}
		
		if (type == Type.BOOLEAN) {
			HTMLs.appendAttribute(sb, "onLabel", Messages.get(MZul.YES));
			HTMLs.appendAttribute(sb, "offLabel", Messages.get(MZul.NO));
		}
		final String fmt = getFormat();
		if (fmt != null && fmt.length() != 0)
			HTMLs.appendAttribute(sb, "z.fmt", fmt);
		return sb.toString();
	}

	public String wrapValue() {
		String stringValue = null;
		if (multiValue) {
			collectionValue = new LinkedList();
			if (value != null) {
				if (value instanceof Collection) 
					collectionValue.addAll((Collection) value);
				else
					collectionValue.add(value);
			}
			JSONArray array;
			if (value instanceof Collection) {
				array = new JSONArray((Collection) value);
			} else {
				array = new JSONArray();
				array.put(value);
			}
			for (int i = 0; i < array.length(); i++) {
				String name = array.optString(i);
				array.put(i, normalize(name));
			}
			stringValue = array.toString();
		} else {
			Object v = normalize(value);
			if (v instanceof String) 
				v = new JSONStringer().valueToString(v);
			if (v != null)
				stringValue = v.toString();
		}
		return stringValue;
	}

	private Object normalize(Object name) {
		if ( type == Type.NAME_DESCRIPTION || type == Type.DESCRIPTION) {
			String description = getDescription((String)name);
			return new JSONArray(new String[] {(String)name, description, description == null? "Wrong value": ""});
		} else if (type == Type.DATE) {
			if (name instanceof Date)
				return new SimpleDateFormat( format ).format((Date)name);
			else if (name instanceof Calendar)
				return new SimpleDateFormat( format ).format(((Calendar)name).getTime());
			else
				return name;
		} else {
			return name;
		}
	}

	@Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final String uuid = getUuid();		
		
		if (type == Type.STRING)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataText\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.DATE)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataDate\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.DESCRIPTION)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataDescription\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.HTML)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataHtml\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.NAME_DESCRIPTION)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataNameDescription\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.LIST)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataList\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.BOOLEAN)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataSwitch\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.IMAGE)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataImage\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
	}

	protected String getRealStyle() {
		StringBuffer sb = new StringBuffer( super.getRealStyle() );

		if ( getMaxlength() > 0)
		{
			HTMLs.appendStyle(sb, "max-width", ""+getMaxlength()+"em");
		}

		return sb.toString();
	}

	
	@Override
	public void setConstraint(String constr) {
		super.setConstraint(constr);
		if (constr.equals("no empty"))
			setSclass(getSclass()+" required");
		else if (getSclass().endsWith(" required"))
			setSclass(getSclass().substring(0, getSclass().length()-9));
	}

	@Override
	protected Object coerceFromString(String value) throws WrongValueException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String coerceToString(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue() {
		return value;
	}

	public boolean isMultivalue() {
		return multiValue;
	}

	public void setMultivalue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getMaxlength() {
		return maxlength == null ? Integer.MAX_VALUE: maxlength.intValue();
	}

	public void setMaxlength(int maxlength) {
		this.maxlength = maxlength;
	}

	@Override
	public String getType() {
		return type.toString();
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setType(String type) {
		this.type = Type.valueOf(type);
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public Command getCommand(String cmdId) {
		if (Events.ON_CHANGE.equals(cmdId))
			return _onChangeCommand;

		if (_onStartSearchCommand.getId().equals(cmdId))
			return _onStartSearchCommand;
		
		if (_onContinueSearchCommand.getId().equals(cmdId))
			return _onContinueSearchCommand;
		
		if (_onCancelSearchCommand.getId().equals(cmdId))
			return _onCancelSearchCommand;
		
		if (_onOpenSelectCommand.getId().equals(cmdId))
			return _onOpenSelectCommand;
		
		return super.getCommand(cmdId);
	}

	private static Command _onChangeCommand = new ComponentCommand(Events.ON_CHANGE, 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			String value = request.getData()[0];
			Integer pos = null;
			if (request.getData().length > 3 && request.getData()[3] != null && !request.getData()[3].isEmpty())
				pos = Integer.parseInt(request.getData()[3]);
			db.onItemChange(value, pos);
		}
	};

	private static Command _onStartSearchCommand = new ComponentCommand("onStartSearch", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			String text = request.getData()[0];
			String cmpId = request.getData()[1];
			db.onStartSearch(text, cmpId);
		}
	};

	private static Command _onContinueSearchCommand = new ComponentCommand("onContinueSearch", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			String cmpId = request.getData()[0];
			db.onContinueSearch(cmpId);
		}
	};

	private static Command _onCancelSearchCommand = new ComponentCommand("onCancelSearch", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			String cmpId = request.getData()[0];
			db.onCancelSearch(cmpId);
		}
	};

	private static Command _onOpenSelectCommand = new ComponentCommand("onOpenSelect", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			Integer pos = null;
			if (request.getData().length > 0 && request.getData()[0] != null && !request.getData()[0].isEmpty())
				pos = Integer.parseInt(request.getData()[0]);
			db.openSelectWindow(pos);
		}
	};

	protected void onItemChange(Object value, Integer pos) {
		if (pos != null && multiValue) {
			while (pos.intValue() >= collectionValue.size())
				collectionValue.add(null);
			collectionValue.set(pos.intValue(), value);
			
			Collection v = new LinkedList();
			for (Object o: collectionValue)
				if (o != null)
					v.add(o);
			this.value = v;
		} else {
			this.value = value;
		}
		
		if (binder.getDataPath() != null)
			binder.setValue(value);

		
		if (type == type.NAME_DESCRIPTION) {
			String description = getDescription(value);
			response("set_description_"+pos, new AuInvoke(this, "setDescription", pos.toString(), description, description == null? "Wrong value": ""));
		}
		Events.postEvent("onChange", this, this.value);
	}

	protected void onCancelSearch(String cmpId) {
		currentSearch = null;
	}

	protected void onContinueSearch(String cmpId) {
		if (cmpId.equals(currentSearch)) {
			List<String[]> l = findNextObjects();
			if (l == null)
			{
				response("continue-search", new AuInvoke(this, "onEndSearchResponse", cmpId));
				currentSearch = null;
			} else {
				JSONArray array = new JSONArray(l);
				response("continue-search", new AuInvoke(this, "onContinueSearchResponse", cmpId, array.toString()));			
			}
		}
	}

	protected void onStartSearch(String text, String cmpId) {
		this.currentSearch = cmpId;
		List<String[]> l = findObjects(text);
		JSONArray array = new JSONArray(l);
		response("start-search", new AuInvoke(this, "onStartSearchResponse", cmpId, array.toString()));
	}

	public void afterCompose() {
		composed = true;
		refreshValue();
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getDescription (Object name) {
		if (name == null)
			return null;
		if ( values != null) {
			for (String v: values) {
				int i = v.indexOf(':');
				if (i > 0)
				{
					if (name.toString().trim().equals(v.substring(0, i).trim()))
						return v.substring(i+1).trim();
				} else if (v.equals(name))
					return "";
			}
			return null;
		} else {
			return "";
		}
	}

	public List<String[]> findObjects(String text) {
		String t [] = text.replaceAll("[-,.]", " ").split(" +");
		List<String[]> data = new LinkedList<String[]>();
		if ( values != null) {
			for (String v: values) {
				if (matches(t, v)) {
					int i = v.indexOf(':');
					if (i > 0)
					{
						data.add(new String[] { v.substring(0, i).trim(),
								v.substring(i+1).trim(),
								type == Type.DESCRIPTION ? v.substring(i+1).trim():  v
						});
					} else {
						data.add(new String[] {v, v, v});
					}
				}
			}
		}
		return data;
	}

	private boolean matches(String[] t, String v) {
		for ( String tt: t) {
			if (! v.toLowerCase().contains(tt.toLowerCase()))
				return false;
		}
		return true;
	}

	public List<String[]> findNextObjects() {
		return null;
	}
	
	public void cancelFindObject() {
	}
	
	public void openSelectWindow(Integer position) {
	}

	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	public String getRemoveIcon() {
		return removeIcon;
	}

	public void setRemoveIcon(String removeIcon) {
		this.removeIcon = removeIcon;
	}

	public String getSelectIcon() {
		return selectIcon;
	}

	public void setSelectIcon(String selectIcon) {
		this.selectIcon = selectIcon;
	}

	public String getWaitIcon() {
		return waitIcon;
	}

	public void setWaitIcon(String waitIcon) {
		this.waitIcon = waitIcon;
	}

	public String getWarningIcon() {
		return warningIcon;
	}

	public void setWarningIcon(String warningIcon) {
		this.warningIcon = warningIcon;
	}

	public void setMaxlength(Integer maxlength) {
		this.maxlength = maxlength;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isDisabled() {
		return disabled;
	}
}

