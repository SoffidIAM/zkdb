package es.caib.zkib.datamodel.xml.handler;

import java.util.Vector;

public class ParsedSQLStatement {
	public String parsedSql;
	public String args[];
	public String getParsedSql() {
		return parsedSql;
	}
	public String[] getArgs() {
		return args;
	}
	public ParsedSQLStatement (String sql) {
		StringBuffer b = new StringBuffer(sql.length());
		Vector<String> v = new Vector<String> ();
		int i = 0;
		do {
			int j  = sql.indexOf("${", i);
			if (j < 0) {
				b.append(sql.substring(i));
				break;
			} else {
				b.append(sql.substring(i, j));
				i = sql.indexOf("}", j);
				if ( i < 0 )
					i = sql.length();
				v.add(sql.substring(j+2, i));
				b.append("?");
				i ++;
				if (i >= sql.length())
					break;
			}
		} while (true);
		parsedSql = b.toString();
		args = v.toArray(new String[v.size()]);
	}
	
}
