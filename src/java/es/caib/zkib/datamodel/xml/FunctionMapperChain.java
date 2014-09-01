package es.caib.zkib.datamodel.xml;

import java.lang.reflect.Method;
import java.util.LinkedList;

import javax.servlet.jsp.el.FunctionMapper;

public class FunctionMapperChain implements FunctionMapper {
	static LinkedList<FunctionMapper> mappers = new LinkedList<FunctionMapper>();
	
	public static void addFunctionMapper (FunctionMapper mapper)
	{
		mappers.add(mapper);
	}
	
	public static void removeFunctionMapper (FunctionMapper mapper)
	{
		mappers.remove(mapper);
	}

	public Method resolveFunction(String arg0, String arg1) {
		for (FunctionMapper mapper: mappers)
		{
			Method m = mapper.resolveFunction(arg0, arg1);
			if (m != null)
				return m;
		}
		return null;
	}

}
