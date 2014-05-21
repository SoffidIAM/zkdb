package es.caib.zkib.datamodel.xml.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.jsp.el.ELException;

import org.zkoss.zk.ui.UiException;

import bsh.EvalError;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.definition.CatchDefinition;
import es.caib.zkib.datamodel.xml.definition.MethodDefinition;

public abstract class AbstractInvokerHandler extends AbstractHandler {
    public AbstractInvokerHandler() {
        super();
    }

    private Method searchMethod(Class clazz, String methodName, Object args[]) {
        Method methods[] = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)
                    && methods[i].getParameterTypes().length == (args == null ? 0
                            : args.length)) {
                Class params[] = methods[i].getParameterTypes();
                boolean ok = true;
                for (int j = 0; ok && j < params.length; j++) {
                    if (args[j] != null
                            && !params[j].isAssignableFrom(args[j].getClass()))
                    {
                    	if (params[j] == long.class && args[j].getClass() == Long.class)
                    		; // OK 
                    	else if (params[j] == int.class && args[j].getClass() == Integer.class)
                    		; // OK 
                    	else if (params[j] == boolean.class && args[j].getClass() == Boolean.class)
                    		; // OK 
                    	else if (params[j] == float.class && args[j].getClass() == Float.class)
                    		; // OK 
                    	else if (params[j] == double.class && args[j].getClass() == Double.class)
                    		; //
                    	else
                    		ok = false;
                    }
                }
                if (ok)
                    return methods[i];
            }
        }
        return null;
    }

    private Method getMethod(Class clazz, String methodName, Object args[]) {
        Class classes[] = null;
        if (args != null) {
            classes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null)
                    classes[i] = args[i].getClass();
            }
        }
        Method m = null;
        try {
            m = clazz.getMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
        }
        if (m == null)
            return searchMethod(clazz, methodName, args);
        else
            return m;
    }

    private Object invokeMethod(Class clazz, Object object, DataContext ctx,
            MethodDefinition methodDefinition) throws Exception {
        Method m;
        String methodName = methodDefinition.getMethod();
        Object args[];
        try {
            args = methodDefinition.evaluateParams(ctx);
        } catch (RuntimeException e1) {
            throw new UiException(e1);
        }

        m = getMethod(clazz, methodName, args);
        if (m == null) {
            String params = "";
            for (int i = 0; args != null && i < args.length; i++) {
                if (i > 0)
                    params = params + ", ";
                if (args[i] == null)
                    params = params + "null";
                else
                    params = params + args[i].getClass().getName();
            }
            throw new UiException("No such method " + getObjectName() + "." + methodName
                    + "(" + params + ")");
        }
        try {
            if (Modifier.isStatic(m.getModifiers()))
                return m.invoke(null, args);
            else
                return m.invoke(object, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                return handleException(ctx, methodDefinition, (Exception) e.getCause());
            } else
                throw e;
        }
    }

    protected Object handleException(DataContext ctx, MethodDefinition methodDefinition, Exception e) throws Exception {
        for (CatchDefinition def: methodDefinition.getCatchs())
        {
            if (e.getClass().getName().equals(def.getException()))
            {
                HashMap m = new HashMap ();
                m.put("exception", e);
                return Interpreter.interpret(ctx, def.getValue(), m);
            }
        }
        throw e;
    }

    public Object invokeMethod(Class clazz, DataContext ctx,
            MethodDefinition methodDefinition) throws Exception {
        return invokeMethod(clazz, null, ctx, methodDefinition);
    }

    public Object invokeMethod(Object obj, DataContext ctx,
            MethodDefinition methodDefinition) throws Exception {
        return invokeMethod(obj.getClass(), obj, ctx, methodDefinition);
    }

    /**
     * @return Returns the jndi.
     */
    protected abstract String getObjectName();
}
