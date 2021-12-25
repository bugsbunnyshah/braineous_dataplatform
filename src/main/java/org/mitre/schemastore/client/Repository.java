package org.mitre.schemastore.client;

import java.net.URI;

/** Class for storing a repository */
public class Repository
{
	// Constants defining the various types of repositories
	public static final Integer SERVICE = 0;
	public static final Integer POSTGRES = 1;
	public static final Integer DERBY = 2;
	
	/** Stores the repository name */
	private String name = null;
	
	/** Stores the type of repository */
	private Integer type = null;
	
	/** Stores the repository location */
	private URI uri = null;
	
	// Stores the database information
	private String databaseName = null;
	private String databaseUser = null;
	private String databasePassword = null;
	
	/** Constructs an unnamed repository */
	public Repository(Integer type, URI uri, String databaseName, String databaseUser, String databasePassword)
		{ this.type=type; this.uri=uri; this.databaseName=databaseName; this.databaseUser=databaseUser; this.databasePassword=databasePassword; }
	
	/** Constructs a named repository */
	public Repository(String name, Integer type, URI uri, String databaseName, String databaseUser, String databasePassword)
		{ this.name=name; this.type=type; this.uri=uri; this.databaseName=databaseName; this.databaseUser=databaseUser; this.databasePassword=databasePassword; }
	
	/** Returns the repository name */
	public String getName()
		{ return name; }
	
	/** Returns the repository type */
	public Integer getType()
		{ return type; }

	/** Returns the repository location */
	public URI getURI()
		{ return uri; }
	
	/** Returns the database name */
	public String getDatabaseName()
		{ return databaseName; }
	
	/** Returns the database user */
	public String getDatabaseUser()
		{ return databaseUser; }
	
	/** Returns the database password */
	public String getDatabasePassword()
		{ return databasePassword; }
	
	/** Displays the name of the repository */
	public String toString()
		{ return name; }
}