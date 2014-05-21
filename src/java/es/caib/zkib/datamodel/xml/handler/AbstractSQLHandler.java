package es.caib.zkib.datamodel.xml.handler;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.jsp.el.ELException;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.Interpreter;

public abstract class AbstractSQLHandler extends AbstractHandler {
	public String jndi;
	public String getJndi() {
		return jndi;
	}

	public void setJndi(String dataSource) {
		this.jndi = dataSource;
	}

	
	public AbstractSQLHandler() {
		super();
	}
	
	protected PreparedStatement createStatement(Connection conn, DataContext ctx, ParsedSQLStatement sql)
			throws NamingException, SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql.getParsedSql());
		String args[] = sql.getArgs();
		
		for (int i = 0; i < args.length;i ++ ) {
			Object v;
			try {
				v = Interpreter.evaluate(ctx, args[i]);
			} catch (ELException e) {
				throw new SQLException("Error evaluating: "+args[i], e);
			}
			if (v == null) {
				stmt.setNull(i+1, Types.NULL);
			} else  if (v instanceof Integer) {
				stmt.setInt(i+1, ((Integer) v).intValue());
			} else  if (v instanceof Long) {
				stmt.setLong(i+1, ((Long) v).longValue());
			} else  if (v instanceof String) {
				stmt.setString(i+1, ((String) v));
			} else  if (v instanceof Boolean) {
				stmt.setBoolean(i+1, ((Boolean) v).booleanValue());
			} else  if (v instanceof Date) {
				stmt.setDate(i+1, (Date) v);
			} else  if (v instanceof java.util.Date) {
				stmt.setDate(i+1, new Date(((Date) v).getTime()));
			} else {
				stmt.setObject(i+1, v);
			}
		}
		return stmt;
	}

	protected Connection getConnection() throws NamingException, SQLException {
		Context nameCtx = new InitialContext();
		javax.sql.DataSource ds = (javax.sql.DataSource) nameCtx.lookup(getJndi());
		if (ds == null) {
			throw new NamingException("Object ["+nameCtx+"] not found on JNDI");
		}

		Connection conn = ds.getConnection();
		return conn;
	}

}