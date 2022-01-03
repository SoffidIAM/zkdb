package es.caib.zkib.datamodel.xml.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.w3c.dom.Element;

import es.caib.zkib.datamodel.DataContext;
import es.caib.zkib.datamodel.xml.ParseException;

public class SQLHandler extends AbstractSQLHandler implements PersistenceHandler {
	private String update, insert, delete;
	private ParsedSQLStatement updateSQL, insertSQL, deleteSQL;
	
	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
		updateSQL = new ParsedSQLStatement(update);
	}

	public String getInsert() {
		return insert;
	}

	public void setInsert(String insert) {
		this.insert = insert;
		insertSQL = new ParsedSQLStatement(insert);
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
		deleteSQL = new ParsedSQLStatement(delete);
	}

	public SQLHandler() {
		super();
	}
	
	private void execute(DataContext ctx, ParsedSQLStatement s) throws Exception {
		
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = createStatement(conn, ctx, s);
			stmt.execute();
		} finally {
			if (stmt != null) try {stmt.close();} catch (Exception e) {}
			if (conn != null) try {conn.close();} catch (Exception e) {}
		}
	}

	public void doInsert(DataContext node) throws Exception {
		if (insertSQL == null)
			throw new Exception("Insert not allowed");
		else
			execute(node, insertSQL);
	}

	public void doDelete(DataContext node) throws Exception {
		if (updateSQL == null)
			throw new Exception("Update not allowed");
		else
			execute(node, updateSQL);
	}

	public void doUpdate(DataContext node) throws Exception {
		if (deleteSQL == null)
			throw new Exception("Delete not allowed");
		else
			execute(node, deleteSQL);
	}
	public void test(Element element) throws ParseException {
		if (jndi == null)
			throw new ParseException("Datasource is missing", element);
	}

}
