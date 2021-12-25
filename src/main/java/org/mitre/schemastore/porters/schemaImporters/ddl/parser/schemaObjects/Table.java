package org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects;

public class Table extends Element {
	private String description = null;

	public Table(String name) throws Exception {
		setName(name);
	}

	// setters
	public void setDescription(String description) {
		if (description != null) { description = description.trim(); }
		if (description != null && description.length() == 0) { description = null; }
		this.description = description;
	}

	// getters
	public String getDescription() {
		return this.description;
	}
}