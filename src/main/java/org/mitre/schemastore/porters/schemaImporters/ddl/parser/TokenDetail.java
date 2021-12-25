package org.mitre.schemastore.porters.schemaImporters.ddl.parser;

import de.susebox.jtopas.Token;

public class TokenDetail {
	private String value;
	private int type;

	public TokenDetail(String value, int type) {
		// remove the indicators that this is a comment
		if (type == Token.BLOCK_COMMENT) {
			value = value.replaceAll("/\\*", "");
			value = value.replaceAll("\\*/", "");
		}
		if (type == Token.LINE_COMMENT) {
			value = value.replaceAll("--", "");
		}
		value = value.trim();

		// if this is a keyword make it uppercase
		if (type == Token.KEYWORD) { value = value.toUpperCase(); }

		this.value = value;
		this.type = type;
	}

	// getters
	public String getValue() { return this.value; }
	public int getType() { return this.type; }

	public String toString() { return this.value; }
}