package es.caib.zkib.datasource;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;

import es.caib.zkib.binder.BindContext;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.DataNodeCollection;
import es.caib.zkib.events.XPathCollectionEvent;
import es.caib.zkib.events.XPathRerunEvent;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public class XPathUtils {
	public static BindContext getComponentContext (Component c)
	{
		while (c != null)
		{
			if ( c instanceof BindContext )
			{
				BindContext b = (BindContext) c;
				if (b.getXPath() != null )
				{
					return b;
				}
			}
			else
			{
				Object obj = c.getAttribute(BindContext.BINDCTX_ATTRIBUTE, Component.COMPONENT_SCOPE);
				if (obj != null && obj instanceof BindContext)
				{
					BindContext b = (BindContext) obj;
					if (b.getDataSource() != null)
					{
						return b;
					}
					
				}
			}
			c = c.getParent();
		}

		
		return null;
	}

	public static Object getValue (DataSource ds, String xpath) 
	{
		return ds.getJXPathContext().getValue(xpath);
	}
	
	public static void setValue (DataSource ds, String xpath, Object value)
	{
		ds.getJXPathContext().setValue(xpath, value);
	}
	
	public static String concat (String path1, String path2)
	{
		String result;
		if (path2.startsWith("/") && path1.endsWith("/"))
			result = path1 + path2.substring(1);
		else if (path2.startsWith("/") || path1.endsWith("/"))
			result = path1 + path2;
		else
			result = path1 + "/" + path2;
		int i;
		while ( (i = result.indexOf("/./")) >= 0)
			result = result.substring(0, i) + result.substring(i+2);
		while ( result.startsWith("./"))
			result = result.substring(2);
		while ( result.endsWith("/."))
			result = result.substring(0, result.length() - 2);
		if (result.isEmpty())
			result = "/";
		return result;
	}
	public static Object getValue (BindContext ctx, String xpath) 
	{
		return ctx.getDataSource().getJXPathContext().getValue( concat(ctx.getXPath(), xpath));
	}
	
	public static void setValue (BindContext ctx, String xpath, Object obj) 
	{
		ctx.getDataSource().getJXPathContext().setValue( concat(ctx.getXPath(), xpath), obj);
	}
	
	public static void removePath (DataSource ds, String xpath)
	{
		String parentXPath;
		if (xpath.endsWith("]"))
		{
			parentXPath = xpath.substring(0, xpath.lastIndexOf("["));
		}
		else
		{
			parentXPath = xpath.substring(0, xpath.lastIndexOf("/"));
		}
		JXPathContext jxp = ds.getJXPathContext();
		Object objectToRemove = jxp.getValue(xpath);
		Object parent = jxp.getValue(parentXPath);
		if (objectToRemove instanceof DataNode && parent instanceof DataModelCollection)
		{
			DataModelNode dn = (DataModelNode) objectToRemove;
			DataModelCollection dc = (DataModelCollection) parent;
			int i = dc.indexOf(dn);
			
			dn.delete();
			
			ds.sendEvent ( new XPathCollectionEvent (ds, parentXPath, XPathCollectionEvent.DELETED, i ));
		}
		else if (parent instanceof Collection)
		{
			int i = 0;
			for (@SuppressWarnings("rawtypes")
			Iterator it = ((Collection) parent).iterator(); it.hasNext(); i++)
			{
				Object next = it.next();
				if (next == objectToRemove)
				{
					it.remove();
					ds.sendEvent ( new XPathCollectionEvent (ds, parentXPath, XPathCollectionEvent.DELETED,i ));
					break;
				}
			}
		}
		else
		{
			jxp.removePath(xpath);
			ds.sendEvent ( new XPathRerunEvent(ds, parentXPath));
		}
	}
	
	public static String createPath (DataSource ds, String xpath) throws Exception
	{
		String parentXPath;
		JXPathContext jxp = ds.getJXPathContext();
		Pointer pointer = jxp.getPointer(xpath);
		Object container = pointer.getValue();
		String newPath;
		if (container instanceof DataNodeCollection)
		{
			DataNodeCollection dnc = (DataNodeCollection) container;
			dnc.newInstance();
			return pointer.asPath()+"["+dnc.size()+"]";
		}
		else if (container instanceof Collection)
		{
			Collection coll = (Collection) container;
			return pointer.asPath()+"["+(1+coll.size())+"]";
		}
		else
		{
			xpath = jxp.createPath(xpath).asPath();
			ds.sendEvent(new XPathRerunEvent(ds, xpath));
			return pointer.asPath();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String createPath (DataSource ds, String xpath, Object value) throws Exception
	{
		JXPathContext jxp = ds.getJXPathContext();
		Pointer pointer = jxp.getPointer(xpath);
		Object container = pointer.getValue();
		if (container instanceof DataNodeCollection)
		{
			DataNodeCollection dnc = (DataNodeCollection) container;
			dnc.add(value);
			return pointer.asPath()+"["+dnc.getSize()+"]";
		}
		else if (container instanceof Collection)
		{
			Collection coll = (Collection) container;
			boolean changed = coll.add(value);
				
			int pos = -1;
			if (coll instanceof List)
			{
				pos = ((List) coll).indexOf(value); 
				if (pos < 0)
					return null;
			}
			else
			{
				int i = 0;
				for (Iterator it = coll.iterator(); it.hasNext(); i++)
				{
					Object next = it.next();
					if (next == value)
					{
						pos = i;
						break;
					}
				}
			}
			if (pos >= 0)
			{
				if (changed)
					ds.sendEvent ( new XPathCollectionEvent (ds, xpath, XPathCollectionEvent.ADDED, pos ));
				return pointer.asPath()+"["+ Integer.toString(pos+1) + "]";
			}
			else
				return null;
		}
		else
		{
			xpath = jxp.createPath(xpath).asPath();
			ds.sendEvent(new XPathRerunEvent(ds, xpath));
			return pointer.asPath();
		}
	}

}
