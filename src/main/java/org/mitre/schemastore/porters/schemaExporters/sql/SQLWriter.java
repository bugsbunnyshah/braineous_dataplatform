package org.mitre.schemastore.porters.schemaExporters.sql;

/**
 * <p>
 * Description: Writes the RDB schema to in SQL format
 * </p>
 * @author Hao Li
 * @version 2.0
 */

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class SQLWriter {

	protected Rdb _rdb;
	protected StringBuffer _sql;
	protected StringBuffer _cleanup;
	protected StringBuffer _comments;

	protected String COMM_ = "-- ";

	public SQLWriter(Rdb rdb) {
		_rdb = rdb;
		_sql = new StringBuffer();
		_cleanup = new StringBuffer();
		_comments = new StringBuffer();
	}

	public StringBuffer createDB(Rdb rdb) {
		String rdbName = rdb.getName();
		StringBuffer cb = new StringBuffer();
		if (rdbName != null && rdbName.length() > 0) {
			cb.append("CREATE DATABASE ");
			cb.append(dbSpecName(rdb.getName()));
			cb.append("; \n");
		}
		return cb;
	}

	// returns database specific name. For postgres, the name needs to be
	// wrapped with double quotes,
	// access it uses square brackets.
	public String dbSpecName(String name) {
		return "\"" + name + "\"";
	}

	/**
	 * writes out a generic XML document for the RDB
	 * 
	 * @param out
	 * @return
	 * @throws KBtoDB.XMLWriter.XMLException
	 */
	public StringBuffer serialize() throws IOException {
		ArrayList<Table> relations = _rdb.getRelations();
		ArrayList<ForeignKey> foriegnKeys = _rdb.getForeignKeys();
		ArrayList<View> views = _rdb.getViews();
		ArrayList<ViewReference> viewReferences = _rdb.getViewKeys();
		ArrayList<DomainTable> refTables = _rdb.getReferenceTables();

		_sql.append(createDB(_rdb));

		// DBURDICK: Added CREATE DOMAIN stmts for each Domain
		Iterator<DomainTable> refItr2 = refTables.iterator();
		while (refItr2.hasNext()) {
			DomainTable ref = refItr2.next();
			String domainName = ref.getName().replaceFirst("TABLE_", "");
			String createDomainStmt = new String (" CREATE DOMAIN " + "\"" + domainName +"\"" + " AS varChar(255); \n ");
			_sql.append( createDomainStmt );
			
		}
		
		// serialize tables
		Iterator<Table> relItr = relations.iterator();
		Table rel;
		while (relItr.hasNext()) {
			rel = (Table) relItr.next();
			_sql.append(createTable(rel));
			_comments.append(createComment(rel));
		}

		// serialize domain tables and their values
		Iterator<DomainTable> refItr = refTables.iterator();
		while (refItr.hasNext()) {
			DomainTable ref = refItr.next();
			ArrayList<ReferenceValue> vals = ref.getReferenceValues();
			for (ReferenceValue val : vals)
				_sql.append(insertReferenceValue(val));
		}

		// foreign keys
		Iterator<ForeignKey> keyItr = foriegnKeys.iterator();
		ForeignKey fk;
		int fkCount = 0;
		while (keyItr.hasNext()) {
			fk = (ForeignKey) keyItr.next();
			_sql.append(createForeignKey(fk));
			fkCount++;
		}

		// views
		Iterator<View> viewItr = views.iterator();
		View view;
		while (viewItr.hasNext()) {
			view = (View) viewItr.next();
			_sql.append(createView(view));
		}

		// view references or rules
		Iterator<ViewReference> viewRefItr = viewReferences.iterator();
		ViewReference vk;
		int vkCount = 0;
		while (viewRefItr.hasNext()) {
			vk = (ViewReference) viewRefItr.next();
			_sql.append(createViewReference(vk));
			vkCount++;
		}

		cleanupRDB();

		_sql.append(_comments);
		_sql.append(_cleanup);

		return _sql;

	}

	private String insertReferenceValue(ReferenceValue val) {
		String tbl = val.getDomain().getName();
		// DBURDICK: added double-quotes around domain table names (for consistency)
		return "INSERT INTO " + "\"" + tbl + "\"" + " VALUES ( DEFAULT, '" + val.getValue() + "');\n";
	}

	private String createComment(Table rel) {
		String comment = rel.getComment().replaceAll("\n", COMM_);
		if (comment.trim().length() > 0)
			return COMM_ + "COMMENT ON TABLE " + rel.getName() + " IS '" + comment + "';\n";
		else
			return "";
	}

	private String createComment(RdbAttribute a) {
		String comment = a.getComment().replaceAll("\n", COMM_);
		if (comment.trim().length() > 0)
			return COMM_ + "COMMENT ON COLUMN " + a.getName() + " IS '" + comment + "';\n";
		else
			return "";
	}

	/**
	 * View references are references to an attribute in a view, similar to
	 * foreign keys for tables. These are implemented as rules.
	 * 
	 * @param vk
	 * @return
	 */
	public StringBuffer createViewReference(ViewReference vk) {
		RdbAttribute toAtt = vk.getReferencedAttribute();
		StringBuffer str = new StringBuffer();
		String fromTable = vk.getContainerRelationName();
		String toTable = toAtt.getContainerRelationName();
		String toAttName = toTable + "." + toAtt.getName();
		String vkName = fromTable + "." + vk.getName();
		String[] events = { "INSERT", "UPDATE" };

		StringBuffer conditionStr = new StringBuffer();

		conditionStr.append("SELECT " + toAttName);
		conditionStr.append(" FROM " + toTable);
		conditionStr.append(" WHERE " + toAttName + "=" + vkName);

		for (int i = 0; i < events.length; i++) {
			String ruleName = "r." + events[i] + "." + vk.getName();
			str.append("CREATE RULE " + ruleName + " AS ON ");
			str.append(events[i] + "\n");
			str.append(" TO " + fromTable + "\n");
			str.append(" DO SELECT " + vkName);
			str.append(" FROM " + fromTable + " WHERE EXISTS \n");
			str.append("(" + conditionStr.toString() + ");\n\n");
		}
		return str;
	}

	public String dropTable(Table rel) {
		return "DROP TABLE " + rel.getName() + " CASCADE; \n ";
	}

	public String dropView(View view) {
		return "DROP VIEW " + view.getName() + " CASCADE; \n";
	}

	public void cleanupRDB() throws IOException {
		ArrayList<Table> relations = _rdb.getRelations();
		ArrayList<View> views = _rdb.getViews();

		// drop table statements
		Iterator<Table> relItr = relations.iterator();
		Table rel;
		while (relItr.hasNext()) {
			rel = (Table) relItr.next();
			_cleanup.append(" -- " + dropTable(rel));
		}

		// drop view statements
		Iterator<View> viewItr = views.iterator();
		View view;
		while (viewItr.hasNext()) {
			view = (View) viewItr.next();
			_cleanup.append(" -- " + dropView(view));
		}
	}

	public void serialize(PrintStream ostream) throws IOException {
		serialize();
		ostream.println(_sql);
	}

	public void serialize(PrintWriter writer) throws IOException {
		serialize();
		writer.write(_sql.toString());
	}

	public void serialize(PrintWriter writer, boolean cleanRDB) throws IOException {
		if (cleanRDB)
			cleanupRDB();
		writer.write(_sql.toString());
	}

	public StringBuffer createForeignKey(ForeignKey fk) {
		StringBuffer str = new StringBuffer();

		str.append("ALTER TABLE ");
		str.append(dbSpecName(fk.getContainerRelationName()));
		str.append(" ADD CONSTRAINT ");
		// DBURDICK: removed "fk_ prefix" before FK so that FK name == FK referring attribute name
		str.append(dbSpecName(fk.getName()));
		str.append(" FOREIGN KEY (" + dbSpecName(fk.getName()) + ") ");
		str.append(" REFERENCES "
				+ dbSpecName(fk.getReferencedAttribute().getContainerRelationName()));
		str.append(" ON DELETE CASCADE ; \n\n");
		return str;
	}

	public StringBuffer createTable(Table rel) {
		ArrayList<RdbAttribute> attributes = rel.getAttributes();
		StringBuffer s_table = new StringBuffer().append("Create table "
				+ dbSpecName(rel.getName()) + "\n");
		s_table.append("(");

		// iterate through attribute definitions
		for (int i = 0; i < attributes.size(); i++) {
			RdbAttribute a = (RdbAttribute) attributes.get(i);
			if (a.isAssociated())
				s_table.append(defineAttribute(a));
			_comments.append(createComment(a));
		}

		// primary key
		StringBuffer pk = definePrimaryKeys(rel);
		if (pk != null)
			s_table.append(pk);

		s_table.append(")" + ";").append("\n\n");
		return s_table;
	}

	protected String defineAttribute(RdbAttribute a) {
		String s = "";
		String name = dbSpecName(a.getName());

		s += name + " ";
		s += a.getDatabaseTypeString() + " ";
		s += (a.isUnique()) ? " UNIQUE " : " ";
		s += (a.isRequired()) ? " NOT NULL " : " ";
		s += ", ";
		s += "\n";
		return s;
	}

	protected StringBuffer definePrimaryKeys(Table rel) {
		ArrayList<RdbAttribute> keys = rel.getPrimaryKeySet();
		StringBuffer s = new StringBuffer().append(" PRIMARY KEY ");
		StringBuffer s_key = new StringBuffer();

		if (keys.size() == 0)
			return new StringBuffer();

		for (int i = 0; i < keys.size(); i++) {
			s_key.append(dbSpecName(((RdbAttribute) keys.get(i)).getName()));
			if (i < keys.size() - 1)
				s_key.append(",");
		}

		s.append("(" + s_key + ")");
		return s;
	}

	public StringBuffer createView(View view) {
		StringBuffer str = new StringBuffer();
		str.append("CREATE VIEW " + view.getName() + " AS ");

		ArrayList<RdbAttribute> attributes = view.getAttributes();
		StringBuffer select = new StringBuffer().append(" SELECT ");
		for (int i = 0; i < attributes.size(); i++) {
			RdbAttribute a = (RdbAttribute) attributes.get(i);
			if (!a.isAssociated())
				continue;

			select.append(a.getName() + ",");
		}
		select.deleteCharAt(select.lastIndexOf(","));

		ArrayList<Table> unionTables = view.getUnionRelations();
		String viewName;
		for (int i = 0; i < unionTables.size(); i++) {
			Table table = (Table) unionTables.get(i);
			if (table == null) {
				System.err.println(view.getName() + " union found null table");
				continue;
			}

			viewName = table.getName();
			str.append("\n\t" + select + " FROM " + viewName);
			if (i < (unionTables.size() - 1))
				str.append(("\n UNION "));
		}
		str.append(";\n\n");
		return str;
	}
}
