package org.mitre.schemastore.porters.schemaImporters.ddl.parser.schemaObjects;

public class Element {
	protected String name = null;

	public void setName(String name) throws Exception {
		if (name == null) {
			throw new Exception("Could not create element. No element name specified.");
		}
		this.name = name.toUpperCase();
	}

	public String getName() {
		return name;
	}
}
