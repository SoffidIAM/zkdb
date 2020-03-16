package es.caib.zkib.datasource;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.render.SmartWriter;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.DataNodeCollection;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.events.XPathSubscriber;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Variables;

public abstract class AbstractDataSource extends org.zkoss.zk.ui.AbstractComponent implements
		DataSource {

	private RootDataSourceImpl dsImpl = null;
	private DataSourceVariables variables = new DataSourceVariables ();

	public AbstractDataSource() {
		super();
		// TODO Auto-generated constructor stub
	}

	public abstract Object getData();

	public JXPathContext getJXPathContext() {
		return getDsImpl().getJXPathContext();
	}

	public void subscribeToExpression(String path, XPathSubscriber subscriber) {
		getDsImpl().subscribeToExpression(path, subscriber);
	}

	public void unsubscribeToExpression(String path, XPathSubscriber subscriber) {
		getDsImpl().unsubscribeToExpression(path, subscriber);
	}

	public void sendEvent(XPathEvent event) {
		getDsImpl().sendEvent(event);
	}

	protected RootDataSourceImpl getDsImpl() {
		if (dsImpl == null)
			dsImpl = new RootDataSourceImpl (this,getData());
		return dsImpl;
	}

	public void redraw(Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final String uuid = getUuid();		

		wh.write("<span id=\"").write(uuid).write("\" z.type=\"zul.datasource.Datasource\"")
		.write(">")
		.write("</span>");
	}

	public Variables getVariables() {
		return variables;
	}

	public String getComponentContext (Component c)
	{
		BindContext ctx = XPathUtils.getComponentContext(c);
		if (ctx != null)
			return ctx.getXPath();
		else
			return null;
	}
	
	public Object getValue (String xpath) 
	{
		return getJXPathContext().getValue(xpath);
	}
	
	public void setValue (String xpath, Object value)
	{
		getJXPathContext().setValue(xpath, value);
	}
	
	public void removePath (String xpath)
	{
		XPathUtils.removePath(this, xpath);
	}
	
	public String createPath (String xpath) throws Exception
	{
		return XPathUtils.createPath(this, xpath);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String createPath (String xpath, Object value) throws Exception
	{
		return XPathUtils.createPath(this, xpath, value);
	}

	public void setFocus(String xpath) {
		getDsImpl().focus (xpath);
	}

}
