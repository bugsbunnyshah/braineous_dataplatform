package org.mitre.rmap.model.exports;

import org.mitre.rmap.generator.SQLGenerator;

public class Derby implements Export {
	public String getOpeningSQL() {
		return null;
	}

	public String getCreateTableOptions() {
		return new String(SQLGenerator.DELIM + "skid" + SQLGenerator.DELIM +" int GENERATED ALWAYS AS IDENTITY ,");
	}

	public String getClosingSQL() {
		return null;
	}
}
