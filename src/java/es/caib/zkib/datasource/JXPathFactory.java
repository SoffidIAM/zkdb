package es.caib.zkib.datasource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataModelCollection;
import es.caib.zkib.jxpath.AbstractFactory;
import es.caib.zkib.jxpath.JXPathContext;
import es.caib.zkib.jxpath.Pointer;

public class JXPathFactory extends AbstractFactory {
	static private JXPathFactory theFactory = null;
	
	static public JXPathFactory getInstance()
	{
		if (theFactory == null)
			theFactory = new JXPathFactory();
		return theFactory;
	}
	/* (non-Javadoc)
	 * @see es.caib.zkib.jxpath.AbstractFactory#createObject(es.caib.zkib.jxpath.JXPathContext, es.caib.zkib.jxpath.Pointer, java.lang.Object, java.lang.String, int)
	 */
	public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
		
		if (parent instanceof DataModelCollection)
		{
			try {
				if (index == ((DataModelCollection) parent).getSize())
				{
					((DataModelCollection) parent).newInstance();
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
		if (parent instanceof DataModelNode)
		{
			try {
				DataModelCollection ulm = ((DataModelNode) parent).getListModel(name);
				if ( ulm != null && index == ulm.getSize())
					ulm.newInstance();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		if (parent instanceof Node)
		{
			Node parentNode = (Node) parent;
			NodeList list = parentNode.getChildNodes();
			int contador = 0;
			for (int i = 0; i < list.getLength(); i++ )
			{
				Node n = list.item(i);
				if (n instanceof Element && ((Element)n).getTagName().equals(name))
				{
					contador ++;
				}
			}
			if (contador == index)
			{
				Document doc = parentNode.getOwnerDocument();
				parentNode.appendChild(doc.createElement(name));
				return true;
			}
		}
		return false;
	}

	public JXPathFactory() {
		super();
	}

}
