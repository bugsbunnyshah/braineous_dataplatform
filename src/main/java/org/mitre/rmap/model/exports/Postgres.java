package org.mitre.rmap.model.exports;

import org.mitre.rmap.generator.SQLGenerator;

public class Postgres implements Export {
	public String getOpeningSQL() {
		return new String("CREATE TEMPORARY SEQUENCE seqImport;");
	}

	public String getCreateTableOptions() {
		return new String(SQLGenerator.DELIM + "skid" + SQLGenerator.DELIM + " INTEGER PRIMARY KEY DEFAULT NEXTVAL('" + "seqImport" + "'),");
	}

	public String getClosingSQL() {
		return new String("DROP SEQUENCE seqImport;");
	}
}
