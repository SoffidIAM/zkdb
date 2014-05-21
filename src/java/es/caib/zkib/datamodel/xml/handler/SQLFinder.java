package es.caib.zkib.datamodel.xml.handler;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.jsp.el.ELException;

import org.omg.CosNaming.NamingContext;
import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;
import es.caib.zkib.datamodel.xml.ParseException;
import es.caib.zkib.datamodel.xml.definition.MethodDefinition;
import es.caib.zkib.datamodel.xml.definition.ParameterDefinition;
import es.caib.zkib.datasource.DataSource;

public class SQLFinder extends AbstractSQLHandler implements FinderHandler {
	private String sql;
	private ParsedSQLStatement parsedStatement;
	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
		parsedStatement = new ParsedSQLStatement(sql);
	}
	

	public SQLFinder() {
		super();
	}
	
	public Collection find(DataContext ctx) throws Exception {
		
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = createStatement(conn, ctx, parsedStatement);
			
			rset = stmt.executeQuery();
			ResultSetMetaData meta = rset.getMetaData();
			Collection<HashMap<String, Object>> col = new LinkedList<HashMap<String,Object>>();
			while (rset.next())
			{
				HashMap<String, Object> m = new HashMap<String, Object>();
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					m.put(meta.getColumnName(i), rset.getObject(i));
				}
				col.add(m);
			}
			
			return col;
		} finally {
			if (rset != null) try {rset.close();} catch (Exception e) {}
			if (stmt != null) try {stmt.close();} catch (Exception e) {}
			if (conn != null) try {conn.close();} catch (Exception e) {}
		}
	}
	
	public void test(Element element) throws ParseException {
		if (sql == null)
			throw new ParseException ("SQL sentence is missing", element);
		if (jndi == null)
			throw new ParseException("Datasource is missing", element);
	}

}
