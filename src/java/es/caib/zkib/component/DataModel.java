package es.caib.zkib.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.beanutils.ConstructorUtils;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.ext.AfterCompose;

import es.caib.zkib.datamodel.AbstractDataModel;
import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.xml.CustomData;
import es.caib.zkib.datamodel.xml.Parser;
import es.caib.zkib.datamodel.xml.XmlDataNode;
import es.caib.zkib.datamodel.xml.definition.ModelDefinition;


public class DataModel extends AbstractDataModel implements AfterCompose {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7714208746485061509L;
	private transient DataNode data = null;
	private String className;
	private String src;
	private String root;


	public DataModel() {
		super();
	}

	public DataModelNode getDataNode ()
	{
		if ( data == null)
		{
			if (className != null)
			{
				Class clazz;
				try {
					clazz = Class.forName(className);
				} catch ( ClassNotFoundException e ) {
					throw new UiException ("Class "+className+" not found");
				}
				if (clazz == null) 
					throw new UiException ("Class "+className+" not found");
				try {
					DataContext newCtx = new DataContext ();
					newCtx.setDataSource(this);
					newCtx.setData(new Object ());
					data = (DataNode) ConstructorUtils.invokeConstructor(clazz, newCtx);
					newCtx.setCurrent(data);
				} catch (Exception e) {
					throw new RuntimeException (e);
				}
			}
			else if (src != null)
			{
				if (root == null)
					throw new UiException ("Root not especified for DataSource");
				Parser parser = new Parser ();
				InputStream input = getDesktop().getWebApp().getResourceAsStream(src);
				if ( input == null)
					input = Thread.currentThread().getContextClassLoader().getResourceAsStream(src);
				if ( input == null)
					throw new UiException ("XML file not found "+src);
				ModelDefinition def;
				try {
					def = parser.parse(input);
				} catch (Exception e) {
					throw new UiException ("Error parsing file "+src, e);
				} finally {
					try {
						input.close();
					} catch (IOException e) {
					}
				}
				CustomData info = new CustomData ();
				info.setModel(def);
				info.setType(root);
				DataContext newCtx = new DataContext ();
				newCtx.setDataSource(this);
				newCtx.setData("");
				newCtx.setCustomData(info);
				data = new XmlDataNode(newCtx);
				newCtx.setCurrent(data);
				return data;
			}
			else
				throw new UiException ("Attribute class or src not especified");
		}
		return data;
	}
	
	
	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className The className to set.
	 */
	public void setClass(String className) {
		this.className = className;
	}

	/**
	 * @return Returns the src.
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * @param src The src to set.
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * @return Returns the root.
	 */
	public String getRootNode() {
		return root;
	}

	/**
	 * @param root The root to set.
	 */
	public void setRootNode(String root) {
		this.root = root;
	}

	public String getRootPath() {
		return "/";
	}

	public void afterCompose() {
		Map atts = getAttributes();
		for (Object att: atts.keySet()) {
			Object value = atts.get(atts);
			getJXPathContext().getVariables().declareVariable((String)att, value);
		}
	}
}
