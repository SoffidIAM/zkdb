package es.caib.zkib.datamodel.xml;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.FunctionMapper;

import org.apache.commons.el.ExpressionEvaluatorImpl;

import bsh.EvalError;
import bsh.TargetError;
import es.caib.zkib.datamodel.DataContext;

public class Interpreter {
	static ThreadLocal interpreter = new ThreadLocal();
	static ExpressionEvaluatorImpl evaluator = null;
	private static FunctionMapper functionMapper = new FunctionMapperChain();

	public Interpreter() {
		super();
	}

	public static Object interpret(DataContext ctx, String script)
			throws EvalError {
		return interpret(ctx, script, null);
	}

	public static Object interpret(DataContext ctx, String script, Map env)
			throws EvalError {
		bsh.Interpreter i = (bsh.Interpreter) interpreter.get();
		if (i == null) {
			i = new bsh.Interpreter();
			interpreter.set(i);
		}

		if (env != null)
			for (java.util.Iterator it = env.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				i.set(key, env.get(key));
			}
		i.set("self", ctx.getCurrent());
		i.set("instance", ctx.getData());
		i.set("parent", ctx.getParent());
		i.set("datasource", ctx.getDataSource());
		try {
			return i.eval(script);
			
		} catch (TargetError e) {
			throw new RuntimeException(e.getTarget());
		}
	}

	public static Object evaluate(DataContext ctx, String expression)
			throws ELException {
		return evaluate(ctx, expression, Object.class);
	}

	public static Object evaluate(DataContext ctx, String expression,
			Class clazz) throws ELException {
		if (evaluator == null)
		{
			evaluator = new ExpressionEvaluatorImpl();
		}
		return evaluator.evaluate(expression, clazz, new VariableResolver(ctx),
				functionMapper);
	}
}
