package org.mitre.schemastore.porters.schemaExporters.sql;

public class NoRelationFoundException extends Exception
{

	static final long serialVersionUID = 1L;

	public NoRelationFoundException() {
		super();
	}

	public NoRelationFoundException(String relation) {
		super( "NoRelationFoundException [" + relation + "]" );
	}

	public NoRelationFoundException(String table, String foreignKey) {
		super( "No Referenced Relation [" + table + "] found for foreign key [" + foreignKey + " ]" );

	}

}
