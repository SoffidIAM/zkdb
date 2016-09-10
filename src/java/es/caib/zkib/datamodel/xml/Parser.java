package es.caib.zkib.datamodel.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.zkoss.zk.ui.Executions;

import es.caib.zkib.datamodel.xml.definition.DefinitionInterface;
import es.caib.zkib.datamodel.xml.definition.ModelDefinition;
import es.caib.zkib.datamodel.xml.handler.FinderHandler;
import es.caib.zkib.datamodel.xml.handler.PersistenceHandler;

public class Parser {
	Properties props = null;
	HashMap persistencers = new HashMap ();
	HashMap finders = new HashMap ();
	HashMap instancers = new  HashMap ();
	Document doc;
	public Parser()  {
	}
	
	public ModelDefinition parse (InputStream is) throws ParseException, ParserConfigurationException, SAXException, IOException
	{
		if (props == null)
		{
			props = new Properties();
			InputStream in = getClass().getResourceAsStream("/metainfo/zkib/parser.properties");
			if (in == null)
				throw new IOException ("Unable to get resource: metainfo/zkib/parser.properties");
			props.load(in);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.parse(is);
		DefinitionInterface model = parse (doc.getDocumentElement());
		if (! (model instanceof ModelDefinition))
			throw new ParseException ("Root tag must be a zkib-model tag", doc.getDocumentElement());
		return (ModelDefinition) model;
	}

	private DefinitionInterface parse (Element element) throws ParseException {
		DefinitionInterface def;
		try {
			def = newInstance (element.getTagName());
		} catch (InstantiationException e) {
			throw new ParseException ("Cannot instantiate", element, e);
		} catch (IllegalAccessException e) {
			throw new ParseException ("Cannot instantiate", element, e);
		}
		if (def == null)
			throw new ParseException ("Unknown element", element);
		applyProperties(def, element);
		def.test(element);
		return def;
		
	}
	

	private void applyProperties (Object bean, Element element) throws ParseException
	{
		WrapDynaBean dynaBean = new WrapDynaBean(bean);
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++)
		{
			String attribute = attributes.item(i).getNodeName();
			String value = attributes.item(i).getNodeValue();
			try {
				applyProperty(dynaBean, attribute, value);
			} catch (Exception e) {
				throw new ParseException ("Unable to set "+attribute+"="+value,element);
			}
		}
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
			{
				Element child = (Element) children.item(i);
				String name;
				String value;
				String map = null;
				if ("attribute".equals(child.getTagName()))
				{
					name = child.getAttribute("name");
					if (name == null)
						throw new ParseException("Attribute 'name' not found",element);
					value = child.getAttribute("value");
					if (value == null)
						value = getElementText(child);
					try {
						applyProperty(dynaBean, name, value);
					} catch (Exception e) {
						throw new ParseException ("Unable to set "+name+"="+value,element);
					}
				}
				else 
				{
					DefinitionInterface childDef = parse (child);
					Method methods [] = bean.getClass().getMethods();
					boolean ok = false;
					for (int j = 0; !ok && j < methods.length; j++)
					{
						if (methods[j].getName().equals("add") &&
							methods[j].getParameterTypes().length == 1 &&
							methods[j].getParameterTypes()[0].isAssignableFrom(childDef.getClass()))
						{
							try {
								methods[j].invoke(bean, new Object[] { childDef} );
								ok = true;
							} catch (Exception e) {
								throw new ParseException ("Element "+child.getTagName()+" not allowed ",element);
							}
						}
					}
					if (!ok)
						throw new ParseException ("Element "+child.getTagName()+" not allowed ",element);
				}
			}
			else if (children.item(i).getNodeType() == Node.CDATA_SECTION_NODE)
			{
				CharacterData cdata = (CharacterData) children.item(i);
				String value = cdata.getData();
				if (value.length() > 0)
				{
					try {
						dynaBean.set("value", value);
					} catch (Exception e) {
						throw new ParseException ("Unable to set value "+value,element);
					}
				}
			}
		}

		String value = getElementText(element);
		if (value.length() > 0)
		{
			try {
				dynaBean.set("value", value);
			} catch (Exception e) {
				throw new ParseException ("Unable to set value "+value,element);
			}
		}
	}

	private void applyProperty(WrapDynaBean dynaBean, String attribute,
			String value) {
		DynaProperty prop = dynaBean.getDynaClass().getDynaProperty(attribute);
		if (prop != null && prop.getType() == Boolean.class)
			dynaBean.set(attribute, Boolean.valueOf(value));
		else if (prop != null && prop.getType() == Boolean.TYPE)
			dynaBean.set(attribute, Boolean.valueOf(value).booleanValue());
		else if (prop != null && prop.getType() == Long.class)
			dynaBean.set(attribute, Long.valueOf(value));
		else if (prop != null && prop.getType() == Integer.class)
			dynaBean.set(attribute, Integer.valueOf(value));
		else if (prop != null && prop.getType() == Double.class)
			dynaBean.set(attribute, Double.valueOf(value));
		else
			dynaBean.set(attribute, value);
	}

	/**
	 * @param child
	 * @return
	 * @throws ParseException
	 */
	private String getElementText(Element child) throws ParseException {
		String text = "";
		for (int j = 0; j < child.getChildNodes().getLength(); j++)
		{
			Node rechild = child.getChildNodes().item(j);
			if ( rechild.getNodeType() == Node.TEXT_NODE)
				text = text + rechild.getNodeValue();
		}
		return text.trim();
	}

	public PersistenceHandler [] getPersistenceHandlers (String name)
	{
		return null;
	}
	
	public FinderHandler [] getFinderHandlers (String objectName, String finderName)
	{
		return null;
	}
	
	private Class instanceClass (String tag)
	{
		String className = props.getProperty(tag);
		if (className != null)
		{
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private DefinitionInterface newInstance (String tag) throws InstantiationException, IllegalAccessException
	{
		Class c = instanceClass (tag);
		if (c == null)
			return null;
		return (DefinitionInterface) c.newInstance();
	}
}
