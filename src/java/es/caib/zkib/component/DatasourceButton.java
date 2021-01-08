package es.caib.zkib.component;

import java.io.IOException;
import java.io.Writer;import javax.xml.xpath.XPathConstants;

import org.zkoss.xml.HTMLs;
import org.zkoss.xml.XMLs;
import org.zkoss.zk.au.out.AuScript;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Button;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.datasource.DataSource;

public class DatasourceButton extends Button  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -530199585286130616L;
	private String dataModel;
	private boolean _created = false;
	private boolean onlyIcon = false;
	
	public DatasourceButton ()
	{
//		setImage ("~./img/document-save.gif"); //$NON-NLS-1$
		addEventListener(Events.ON_CREATE, new EventListener () {
			public boolean isAsap() {
				return false;
			}
			public void onEvent(Event event) {
				if ( dataModel != null)
				{
					_created = true;
					installHooks ();
				}
			}
		});
		
	}

	public String getDatamodel() {
		return dataModel;
	}

	public void setDatamodel(String listbox) {
		this.dataModel = listbox;
		if (!_created)
		{
			installHooks();
		} else {
			if (listbox != null)
			{
				Component c = getDatamodelComponent();
				if (c != null)
					smartUpdate("dsid", c.getUuid());
				else
					smartUpdate("dsid", null);
			}
			else
				smartUpdate("dsid", null);
		}
		
	}
	
	protected void installHooks() {
		Component c = Path.getComponent(getSpaceOwner(), dataModel);
		if (c != null && c instanceof DataModel)
		{
			c.addEventListener("onChange", new EventListener () //$NON-NLS-1$
			{
				public boolean isAsap() {return false;}
				public void onEvent(org.zkoss.zk.ui.event.Event event) {
					checkCommitPending ();
				};
			});
			c.addEventListener("onCommit", new EventListener () //$NON-NLS-1$
			{
					public boolean isAsap() {return false;}
					public void onEvent(org.zkoss.zk.ui.event.Event event) {
						checkCommitPending ();
					};
			});
		}
		checkCommitPending();
	}
	

	protected void checkCommitPending() {
		Component c = null;
		try {
			c = Path.getComponent(getSpaceOwner(), dataModel);
		} catch (ComponentNotFoundException e) {
		}
		if (c != null && c instanceof DataSource)
		{
			DataSource dataSource = (DataSource) c;
			setVisible(true);
			if (dataSource.isCommitPending())
				Executions.getCurrent().addAuResponse("unableExit", new AuScript(this , "zkau.confirmClose=true;"));
			else
				Executions.getCurrent().addAuResponse("unableExit", new AuScript(this , "zkau.confirmClose=false;"));
			setDisabled(! dataSource.isCommitPending() );
		}
		else
		{
			setVisible(false);
		}
	}

	@Override
	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final String uuid = getUuid();		

		if (! isImageAssigned() && Executions.getCurrent().isSafari())
		{
			wh.write("<button type='button' id=\"").write(uuid).write("\" z.type=\"zul.datasource.CommitButton\"")
			.write(getOuterAttrs())
			.write(getInnerAttrs())
			.write(">");
			wh.write(XMLs.encodeText(getLabel()));
			wh.write("</button>");
		}
		else
		{
			wh.write("<button type='button' id=\"").write(uuid).write("\" z.type=\"zul.datasource.CommitButton\"")
			.write(getOuterAttrs())
			.write(getInnerAttrs())
			.write(">");
			if ("reverse".equals(getDir()))
			{
				if ( !onlyIcon) {
					wh.write(XMLs.encodeText(getLabel()));
					if("vertical".equals(getOrient()))
						wh.write("<br/>");
				}
				wh.write(getImgTag());
			} else {
				wh.write(getImgTag());
				if ( !onlyIcon) {
					if("vertical".equals(getOrient()))
						wh.write("<br/>");
					wh.write(XMLs.encodeText(getLabel()));
				}
			}
			wh.write("</button>");
		}
	}

	public String getOuterAttrs() {
		final StringBuffer sb =
			new StringBuffer(64).append(super.getOuterAttrs());
		if (dataModel != null)
		{
			Component ds = getDatamodelComponent();
			if (ds != null && ds instanceof Component)
				HTMLs.appendAttribute(sb, "dsid", ((Component)ds).getUuid());
				
		}
		if (onlyIcon) {
			HTMLs.appendAttribute(sb, "title", getLabel());
		}
		return sb.toString();
	}

	public Component getDatamodelComponent() {
		String path = dataModel;
		if (path.startsWith("/") && ! path.startsWith("//"))
		    path = "//"+getPage().getId()+path;
		Component ds = Path.getComponent(getSpaceOwner(), path);
		while (ds instanceof BindContext) {
			DataSource parent = ((BindContext)ds).getDataSource();
			if (parent == null) return ds;
			ds = (Component) parent;
		}
		return ds;
	}

	public boolean isOnlyIcon() {
		return onlyIcon;
	}

	public void setOnlyIcon(boolean onlyIcon) {
		if (onlyIcon)
			setSclass("icon-button");
		this.onlyIcon = onlyIcon;
	}


}
