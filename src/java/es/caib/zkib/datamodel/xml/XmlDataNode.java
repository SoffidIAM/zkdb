package es.caib.zkib.datamodel.xml;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.jsp.el.ELException;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bsh.EvalError;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.DataModelNode;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.DataNodeCollection;
import es.caib.zkib.datamodel.xml.definition.CustomAttributeDefinition;
import es.caib.zkib.datamodel.xml.definition.FinderDefinition;
import es.caib.zkib.datamodel.xml.definition.ModelDefinition;
import es.caib.zkib.datamodel.xml.definition.NodeDefinition;
import es.caib.zkib.datamodel.xml.handler.LoadParentHandler;
import es.caib.zkib.datamodel.xml.handler.PersistenceHandler;
import es.caib.zkib.datamodel.xml.validation.ValidationDefinition;
import es.caib.zkib.events.XPathEvent;
import es.caib.zkib.events.XPathSubscriber;

public class XmlDataNode extends DataNode {
	DataContext ctx;
	Vector updateHandlers;
	NodeDefinition definition ;
	
	@SuppressWarnings("unchecked")
	public XmlDataNode(DataContext ctx) {
		super(ctx);
		this.ctx = ctx;
		
		// Obener la definición del nodo
		CustomData info = (CustomData) ctx.getCustomData();
		ModelDefinition model = info.getModel(); 
		definition = model.getNode(info.getType());
		if (definition == null)
			throw new RuntimeException ("Unknown type "+info.getType());
		
		
		setTransient(definition.isTransient());
		// Añadir finders
		FinderDefinition finders[] = definition.getFinders();
		
		for (int i = 0; i < finders.length; i++)
		{
			CustomData info2 = new CustomData ();
			
			info2.setModel( model );
			info2.setType(finders[i].getType());
			
			DataNodeFinder f = new DataNodeFinder (ctx, finders[i]);
			addFinder(
				finders[i].getName(),
				f,
				XmlDataNode.class,
				info2
			);
		}
		
		for (CustomAttributeDefinition att: definition.getCustomAttributes())
		{
			DynaProperty dp = new DynaProperty (att.getName(), String.class);
			dynaProperties.add(dp);
			dynaPropertiesMap.put(att.getName(), dp);
			String attPath = normalizePath("@"+att.getName());
			for (String dependency:att.getDependsList())
			{
				getDataSource().subscribeToExpression(
						normalizePath("@"+dependency), 
						new AttributeSubscriber(attPath));
			}
		}
	}
	
	private String normalizePath (String path) 
	{
		String xpath = getXPath()+"/"+path;
		do 
		{
			int i = xpath.indexOf("/./");
			if (i < 0) break;
			
			xpath = xpath.substring(0, i) + xpath.substring(i+2);
		} while (true);
		
		do
		{
			int i = xpath.indexOf("/../");
			if (i < 0) break;
			
			int j = xpath.lastIndexOf("/", i-1);
			if (j < 0) 
				xpath = xpath.substring(i+4);
			else
				xpath = xpath.substring(0, j) + xpath.substring(i+3);
		} while (true);
		
		return xpath;
	}
	
	@Override
	public Object get(String name) {
		CustomAttributeDefinition att = definition.getCustomAttribute(name);
		if (att == null)
			return super.get(name);
		else
		{
			if (att.getExpr() != null)
				try {
					return Interpreter.evaluate(ctx, att.getExpr()).toString();
				} catch (ELException e1) {
					throw new RuntimeException("Error evaluating: "+att.getExpr(), e1);
				}
			else if (att.getValue() != null)
				try {
					return Interpreter.interpret(ctx, att.getValue());
				} catch (EvalError e) {
					throw new RuntimeException(e);
				}
			else
				return null;
		}
	}

	protected void doInsert() throws Exception {
		validate ();
		boolean found = false;
		PersistenceHandler handlers[] = definition.getPersistenceHandlers();
		for (int i = 0; i < handlers.length; i++)
		{
			if (handlers[i].isSuitable(ctx))
			{
				handlers[i].doInsert(ctx);
				found = true;
			}
		}
		if (!found)
			throw new RuntimeException ("Insert not allowed on "+getXPath());
		if (getChildProperty() != null) {
			getContainer().reorderOnTree(this);
		}
	}

	protected void doUpdate() throws Exception {
		validate ();
		boolean found = false;
		PersistenceHandler handlers[] = definition.getPersistenceHandlers();
		for (int i = 0; i < handlers.length; i++)
		{
			if (handlers[i].isSuitable(ctx))
			{
				handlers[i].doUpdate(ctx);
				found = true;
			}
		}
		if (!found)
			throw new RuntimeException ("Update not allowed for object "+getXPath());
		if (getChildProperty() != null) {
			getContainer().reorderOnTree(this);
		}
	}

	protected void validate() throws Exception {
		for (ValidationDefinition validation: definition.getValidations())
		{
			validation.validate(ctx);
		}
	}

	protected void doDelete() throws Exception {
		boolean found = false;
		PersistenceHandler handlers[] = definition.getPersistenceHandlers();
		for (int i = 0; i < handlers.length; i++)
		{
			if (handlers[i].isSuitable(ctx))
			{
				handlers[i].doDelete(ctx);
				found = true;
			}
		}
		if (!found)
			throw new RuntimeException ("Delete not allowed from "+getXPath());
	}

	public String getParentProperty() {
		return definition.getParentProperty();
	}
	
	public String getIdProperty () {
		return definition.getIdProperty();
	}
	
	public String getChildProperty() {
		return definition.getChildProperty();
	}
	
	public Object loadParentObject() throws Exception {
		if (getParentId() != null) {
			for (LoadParentHandler handler: definition.getLoadParentHandlers()) {
				Object parent = handler.loadParent(ctx);
				if (parent != null)
					return parent;
			}
		}
		return null;
	}

	public Object getParentId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (definition.getParentProperty() == null)
			return null;
		Object parent = PropertyUtils.getProperty(ctx.getData(), definition.getParentProperty());
		if (parent == null || parent.toString().isEmpty())
			return null;
		else
			return parent;
	}

	public Object getCurrentId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (definition.getIdProperty() == null)
			return null;
		return PropertyUtils.getProperty(ctx.getData(), definition.getIdProperty());
	}

}

class AttributeSubscriber implements XPathSubscriber 
{
	String attpath;

	public AttributeSubscriber(String attpath) {
		super();
		this.attpath = attpath;
	}

	public void onUpdate(XPathEvent event) {
		event.getDataSource().sendEvent(
				new XPathEvent(event.getDataSource(), attpath));
	}
}