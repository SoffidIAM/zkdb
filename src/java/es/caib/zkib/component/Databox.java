package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONStringer;
import org.zkoss.image.AImage;
import org.zkoss.mesg.Messages;
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
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
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
	protected SingletonBinder binder = new SingletonBinder (this);
	private boolean duringOnUpdate = false;
	private String placeholder;
	private List<String> values;
	private List<String> descriptions;
	protected List collectionValue;
	boolean multiValue;
	boolean required;
	boolean readonly;
	boolean multiline;
	Integer maxlength;
	Integer maxrows;
	boolean composed = false;
	boolean duringClientChange = false;
	boolean hyperlink = false;
	boolean noadd = false;
	boolean noremove = false;
	Integer rows = null;
	String uploadMessage = "Upload";
	String downloadMessage = "Download";
	String clearMessage = "Clear";
	String uploadIcon = null;
	String downloadIcon = null;
	String clearIcon = null;
	boolean fileMenu = false;
	
	public enum Type {
		STRING,
		NUMBER,
		NAME_DESCRIPTION,
		DESCRIPTION,
		DATE,
		BOOLEAN,
		LIST,
		IMAGE,
		HTML,
		PASSWORD,
		BINARY,
		SEPARATOR
	};

	String label;
	Object value;
	
	Type type = Type.STRING;
	String icon;
	String removeIcon = "~./img/remove.png";
	String calendarIcon = "~./zul/img/caldrbtn.gif";
	String selectIcon = null;
	String selectIcon2 = null;
	String waitIcon = "/img/wait.gif"; 
	String warningIcon = "~./img/warning.gif";
	String format;
	boolean forceSelectIcon;

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
	
	public Object translateToUserInterface(Object o) {
		if (type == Type.BINARY) 
			return null;
		return o;
	}

	public Object translateFromUserInterface(Object o) {
		return o;
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
							normalize( translateToUserInterface( value ) ).toString()));
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
		if ( ! duringClientChange && ! duringClientChange)
			refreshValue ();
	}

	protected void refreshValue ()
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
		if (maxrows != null)
			HTMLs.appendAttribute(sb, "maxrows", maxrows);
		if (isDisabled())
			HTMLs.appendAttribute(sb, "disabled", isDisabled());
		if (readonly)
			HTMLs.appendAttribute(sb, "readonly", readonly);
		HTMLs.appendAttribute(sb, "multiline", multiline);
		if (Boolean.TRUE.equals(multiline) && rows != null)
			HTMLs.appendAttribute(sb, "rows", rows);
		
		HTMLs.appendAttribute(sb, "multivalue", multiValue);
		if (multiValue) {
			if (noadd) HTMLs.appendAttribute(sb, "noadd", noadd);
			if (noremove) HTMLs.appendAttribute(sb, "noremove", noremove);
		}
		if (type == Databox.Type.NUMBER)
			HTMLs.appendAttribute(sb, "number", true);
		if (hyperlink)
			HTMLs.appendAttribute(sb, "hyperlink", true);
		if (required)
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
			
		if (type == Type.NAME_DESCRIPTION || type == Type.DESCRIPTION || selectIcon != null)
			HTMLs.appendAttribute(sb, "selecticon", getDesktop().getExecution().encodeURL(selectIcon == null ? "~./img/magnifier.png" : selectIcon));
		if (selectIcon2 != null)
			HTMLs.appendAttribute(sb, "selecticon2", getDesktop().getExecution().encodeURL(selectIcon2));
		if (forceSelectIcon)
			HTMLs.appendAttribute(sb, "forceSelecticon", "true");
		if (values != null && type == Type.LIST)
		{
			JSONArray jsonValues = new JSONArray(values);
			if ( isMultiValue() ||
					isRequired() && (value == null || "".equals(value))) {
				JSONArray array2 = new JSONArray();
				array2.put(":"+"- Select value -");
				for (int i = 0; i < jsonValues.length(); i++)
					array2.put(jsonValues.get(i));
				jsonValues = array2;
			}
			HTMLs.appendAttribute(sb, "values", jsonValues.toString());
		}
		
		if (descriptions != null) {
			HTMLs.appendAttribute(sb, "description", new JSONArray(descriptions).toString());
		}

		if (type == Type.BOOLEAN) {
			HTMLs.appendAttribute(sb, "onLabel", Messages.get(MZul.YES));
			HTMLs.appendAttribute(sb, "offLabel", Messages.get(MZul.NO));
		}
		if (type == Type.BINARY) {
			HTMLs.appendAttribute(sb, "uploadmessage", uploadMessage);
			HTMLs.appendAttribute(sb, "downloadmessage", downloadMessage);
			HTMLs.appendAttribute(sb, "clearmessage", clearMessage);
			HTMLs.appendAttribute(sb, "uploadicon", getDesktop().getExecution().encodeURL( uploadIcon));
			HTMLs.appendAttribute(sb, "downloadicon", getDesktop().getExecution().encodeURL(downloadIcon));
			HTMLs.appendAttribute(sb, "clearicon", getDesktop().getExecution().encodeURL(clearIcon));
			HTMLs.appendAttribute(sb, "filemenu", fileMenu);
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
				array = new JSONArray();
				for (Iterator<Object> it = ((Collection)value).iterator(); it.hasNext();)
					array.put(normalize(  translateToUserInterface( it.next() ) ));
			} else {
				array = new JSONArray();
				array.put( normalize(  translateToUserInterface( value ) ));
			}
			stringValue = array.toString();
		} else {
			Object v = normalize(translateToUserInterface( value ));
			if (v instanceof String) 
				v = new JSONStringer().valueToString(v);
			if (v != null)
				stringValue = v.toString();
		}
		return stringValue;
	}

	private Object normalize(Object name) {
		if (name instanceof Collection) {
			JSONArray a = new JSONArray();
			for ( Object o: (Collection) name)
				a.put( normalize(o));
			return a;
		} else {
			if (type == Type.PASSWORD) {
				if ( name == null || name.toString().trim().isEmpty())
					return "";
				else
					return "*Soffid*Secured*";
			} else if ( type == Type.NAME_DESCRIPTION || type == Type.DESCRIPTION) {
				try {
					if (name == null || name.toString().trim().isEmpty())
						return new JSONArray(new String[] {"", "", ""});
					else {
						String description = getDescription(translateFromUserInterface( name ).toString());
						return new JSONArray(new String[] {name.toString(), description, description == null? "Wrong value": ""});
					}
				} catch (Exception e) {
					return new JSONArray(new String[] {name.toString(), null, e.toString()});
				}
			} else if (type == Type.DATE) {
				if (name instanceof Date) {
					if (format == null)
						return DateFormats.getDateTimeFormat().format((Date)name);
					else {
						SimpleDateFormat df = new SimpleDateFormat( format );
						df.setTimeZone(TimeZones.getCurrent());
						return df.format((Date)name);
					}
				}
				else if (name instanceof Calendar) {
					if (format == null)
						return DateFormats.getDateTimeFormat().format(((Calendar)name).getTime());
					else {
						SimpleDateFormat df = new SimpleDateFormat( format );
						df.setTimeZone(TimeZones.getCurrent());
						return df.format(((Calendar)name).getTime());
					}					
				}
				else
					return name;
			} else if (type == Type.IMAGE) {
				if (name instanceof byte[]) {
					byte[] img = (byte[]) name;
					if (img.length > 10) {
						String sb = img[0] == '<' && img[1] == '?' ? "image/svg+xml" :
							img[0] == 0x77 && img[1] == 0xd8 && img[2] == 0xff ?  "image/jpeg":
							img[0] == 0x89 && img[1] == 0x50 && img[2] == 0x4e ?  "image/png":
								"image/gif";
						return "data:"+sb+";base64," + Base64.getEncoder().encodeToString(img);
					} else 
						return "";
				}
				else
					return "";
			} else if (name != null) {
				return name.toString();
			} else {
				return null;
			}
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
		else if (type == Type.NUMBER)
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
		else if (type == Type.IMAGE)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataImage\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.PASSWORD)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataPassword\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.SEPARATOR)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataSeparator\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
		else if (type == Type.BINARY)
		{
			wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.databox.DataBinary\"")
			.write(getOuterAttrs()).write(getInnerAttrs())
			.write("></div>");
		}
	}

	protected String getRealStyle() {
		StringBuffer sb = new StringBuffer( super.getRealStyle() );

//		if ( getMaxlength() > 0)
//		{
//			HTMLs.appendStyle(sb, "max-width", ""+getMaxlength()+"em");
//		}

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

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getMaxlength() {
		return maxlength == null ? Integer.MAX_VALUE: maxlength.intValue();
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
		
		// Binary commants
		if (_onDownloadCommand.getId().equals(cmdId))
			return _onDownloadCommand;
		if (_onUploadCommand.getId().equals(cmdId))
			return _onUploadCommand;
		if (_onClearCommand.getId().equals(cmdId))
			return _onClearCommand;

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
			db.onItemChange( value , pos);
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

	private static Command _onUploadCommand = new ComponentCommand("onUpload", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			Integer pos = null;
			if (request.getData().length > 0 && request.getData()[0] != null && !request.getData()[0].isEmpty())
				pos = Integer.parseInt(request.getData()[0]);
			db.onUpload(pos);
		}
	};

	private static Command _onDownloadCommand = new ComponentCommand("onDownload", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			Integer pos = null;
			if (request.getData().length > 0 && request.getData()[0] != null && !request.getData()[0].isEmpty())
				pos = Integer.parseInt(request.getData()[0]);
			db.onDownload(pos);
		}
	};

	private static Command _onClearCommand = new ComponentCommand("onClear", 0) {
		@Override
		protected void process(AuRequest request) {
			Databox db = (Databox) request.getComponent();
			Integer pos = null;
			if (request.getData().length > 0 && request.getData()[0] != null && !request.getData()[0].isEmpty())
				pos = Integer.parseInt(request.getData()[0]);
			db.onClear(pos);
		}
	};

	protected void onItemChange(Object value, Integer pos) {
		duringClientChange = true;
		try {
			
			value = parseUiValue(value);
			
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
				binder.setValue(this.value);
			
			
			if (type == type.NAME_DESCRIPTION) {
				try {
					String description = getDescription(value);
					setWarning(pos, description == null? "Wrong value": "");
					response("set_description_"+pos, new AuInvoke(this, "setDescription", pos.toString(), description));
				} catch (UiException e) {
					response("set_description_"+pos, new AuInvoke(this, "setDescription", pos.toString(), ""));
					setWarning(pos, e.getMessage());
				} catch (Exception e) {
					response("set_description_"+pos, new AuInvoke(this, "setDescription", pos.toString(), ""));
					setWarning(pos, e.toString());
				}
			}
			
			if (required && !multiValue && 
					(value == null || value.toString().trim().isEmpty())) {
				setWarning(pos, "Value is required");
			}
			Events.postEvent("onChange", this, this.value);
		} finally {
			duringClientChange = false;
		}
	}

	protected void onUpload(Integer pos) {
	}

	protected void onDownload(Integer pos) {
	}

	protected void onClear(Integer pos) {
	}

	protected Object parseUiValue(Object value) {
		value = translateFromUserInterface(value);
		if (type == Type.DATE && value != null && value instanceof String) {
			if (value.toString().trim().isEmpty())
				value = null;
			else
				try {
					if (format == null)
						value = DateFormats.getDateTimeFormat().parse(value.toString());
					else {
						SimpleDateFormat df = new SimpleDateFormat(format);
						df.setTimeZone(TimeZones.getCurrent());
						value = df.parse(value.toString());
					}
				} catch (ParseException e) {
					throw new UiException(e);
				}
		}
		if (type == Type.BOOLEAN)
			value = value != null && "true".equals(value.toString());
		return value;
	}

	protected void onCancelSearch(String cmpId) {
		currentSearch = null;
	}

	protected void onContinueSearch(String cmpId) {
		if (cmpId.equals(currentSearch)) {
			List<String[]> l;
			try {
				l = findNextObjects();
				if (l == null)
				{
					response(null, new AuInvoke(this, "onEndSearchResponse", cmpId, Labels.getLabel("common.no-results")));
					currentSearch = null;
				} else {
					JSONArray array = new JSONArray(l);
					response(null, new AuInvoke(this, "onContinueSearchResponse", cmpId, array.toString()));			
				}
			} catch (Throwable e) {
				throw new UiException(e);
			}
		}
	}

	protected void onStartSearch(String text, String cmpId) {
		this.currentSearch = cmpId;
		try {
			List<String[]> l = findObjects(text);
			JSONArray array = new JSONArray(l);
			response(null, new AuInvoke(this, "onStartSearchResponse", cmpId, array.toString()));
		} catch (Throwable e) {
			throw new UiException(e);
		}
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

	public String getDescription (Object name) throws Exception {
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

	public List<String[]> findObjects(String text) throws Throwable {
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

	public List<String[]> findNextObjects() throws Throwable {
		return null;
	}
	
	public void cancelFindObject() {
	}
	
	public void openSelectWindow(Integer position) {
		Events.postEvent(new Event("onOpen", this, position));
	}

	public void setWarning (Integer position, String message) {
		smartUpdate("warning_"+position, message);
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

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readOnly) {
		this.readonly = readOnly;
	}

	public boolean isMultiline() {
		return multiline;
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
	}

	public String getCalendarIcon() {
		return calendarIcon;
	}

	public void setCalendarIcon(String calendarIcon) {
		this.calendarIcon = calendarIcon;
	}

	@Override
	public String getText() throws WrongValueException {
		return value == null ? "": value.toString();
	}

	@Override
	public void setText(String value) throws WrongValueException {
		setValue(value);
	}

	public boolean isHyperlink() {
		return hyperlink;
	}

	public void setHyperlink(boolean hyperlink) {
		this.hyperlink = hyperlink;
		smartUpdate("hyperlink", hyperlink);
	}

	public String getSelectIcon2() {
		return selectIcon2;
	}

	public void setSelectIcon2(String selectIcon2) {
		this.selectIcon2 = selectIcon2;
	}

	public boolean isForceSelectIcon() {
		return forceSelectIcon;
	}

	public void setForceSelectIcon(boolean forceSelectIcon) {
		this.forceSelectIcon = forceSelectIcon;
	}

	public boolean isNoadd() {
		return noadd;
	}

	public void setNoadd(boolean noadd) {
		this.noadd = noadd;
	}

	public boolean isNoremove() {
		return noremove;
	}

	public void setNoremove(boolean noremove) {
		this.noremove = noremove;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public String getUploadMessage() {
		return uploadMessage;
	}

	public void setUploadMessage(String uploadMessage) {
		this.uploadMessage = uploadMessage;
	}

	public String getDownloadMessage() {
		return downloadMessage;
	}

	public void setDownloadMessage(String downloadMessage) {
		this.downloadMessage = downloadMessage;
	}

	public String getClearMessage() {
		return clearMessage;
	}

	public void setClearMessage(String clearMessage) {
		this.clearMessage = clearMessage;
	}

	public String getUploadIcon() {
		return uploadIcon;
	}

	public void setUploadIcon(String uploadIcon) {
		this.uploadIcon = uploadIcon;
	}

	public String getDownloadIcon() {
		return downloadIcon;
	}

	public void setDownloadIcon(String downloadIcon) {
		this.downloadIcon = downloadIcon;
	}

	public String getClearIcon() {
		return clearIcon;
	}

	public void setClearIcon(String clearIcon) {
		this.clearIcon = clearIcon;
	}

	public boolean isFileMenu() {
		return fileMenu;
	}

	public void setFileMenu(boolean fileMenu) {
		this.fileMenu = fileMenu;
	}

	public Integer getMaxrows() {
		return maxrows;
	}

	public void setMaxrows(Integer maxrows) {
		this.maxrows = maxrows;
	}
}

