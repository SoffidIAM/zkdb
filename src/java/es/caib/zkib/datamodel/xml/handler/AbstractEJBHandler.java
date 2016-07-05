package es.caib.zkib.datamodel.xml.handler;

import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.jsp.el.ELException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DoubleArrayConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatArrayConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerArrayConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongArrayConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortArrayConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.zkoss.zk.ui.UiException;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.definition.HandlerMethodDefinition;
import es.caib.zkib.datamodel.xml.definition.MethodDefinition;

public abstract class AbstractEJBHandler extends AbstractInvokerHandler {
	private String jndi;
	private Context jndiContext ;
	
	protected String getObjectName() 
	{
		return jndi;
	}

	public AbstractEJBHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected Object getEJB (DataContext ctx)
	{
		if (jndi == null)
		{
			throw new UiException ("JNDI not defined for "+ctx.getCurrent().getXPath());
		}
		try {
			if (jndiContext == null)
				jndiContext = new InitialContext ();
			Object home = jndiContext.lookup(jndi);
			if (home instanceof EJBHome ||
					home instanceof EJBLocalHome)
			{
				Method createMethod = home.getClass().getMethod("create", null);
				Object ejb = createMethod.invoke(home, null);
				return ejb;
			}
			else
				return home;
		} catch (Exception e) {
			throw new UiException (e);
		}
	}

	
	protected Object invokeMethod (DataContext ctx, MethodDefinition methodDefinition)
		throws Exception
	{
		Object ejb = getEJB (ctx);
		return invokeMethod(ejb, ctx, methodDefinition);
	}
	
	protected Object invokeMethod (DataContext ctx, HandlerMethodDefinition methodDefinition)
	throws Exception
	{
		Object obj = invokeMethod(ctx, (MethodDefinition) methodDefinition);
		if (methodDefinition.isReturnBean())
		{
	        ConvertUtils.register(new DoubleConverter(null), Double.class);
	        ConvertUtils.register(new FloatConverter(null), Float.class);
	        ConvertUtils.register(new LongConverter(null), Long.class);
	        ConvertUtils.register(new ShortConverter(null), Short.class);
	        ConvertUtils.register(new IntegerConverter(null), Integer.class);
			BeanUtils.copyProperties(ctx.getData(), obj);
		}
		else
		{
			String property = methodDefinition.getReturnProperty();
			if (property != null)
			{
		        ConvertUtils.register(new DoubleConverter(null), Double.class);
		        ConvertUtils.register(new FloatConverter(null), Float.class);
		        ConvertUtils.register(new LongConverter(null), Long.class);
		        ConvertUtils.register(new ShortConverter(null), Short.class);
		        ConvertUtils.register(new IntegerConverter(null), Integer.class);
				BeanUtils.setProperty(ctx.getData(), property, obj);
			}
		}
		return obj;
	}

	/**
	 * @return Returns the jndi.
	 */
	public String getJndi() {
		return jndi;
	}

	/**
	 * @param jndi The jndi to set.
	 */
	public void setJndi(String jndi) {
		this.jndi = jndi;
	}


}
