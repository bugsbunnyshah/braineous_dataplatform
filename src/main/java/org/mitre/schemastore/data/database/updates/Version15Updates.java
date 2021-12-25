package org.mitre.schemastore.data.database.updates;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Create synonym table. It contains a foreign key to schema, which acts as the
 * thesaurus object. It also contains a synset_id. Terms with different names
 * can be placed in the same synset. thesaurus id , synset id, and synonym name
 * comprise of a unique composite.
 * 
 * @author HAOLI
 * 
 */
public class Version15Updates extends AbstractVersionUpdate
{
	/** Retrieves the version number */
	protected int getVersionUpdateNumber() { return 15; }

	/** Runs the version updates */
	protected void runUpdates() throws SQLException
	{
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("CREATE TABLE synonym (id integer NOT NULL, name character varying(100) NOT NULL, description character varying(4096), element_id integer NOT NULL, schema_id integer NOT NULL)");
		stmt.executeUpdate("ALTER TABLE synonym ADD CONSTRAINT synonym_pkey PRIMARY KEY (id)");
		stmt.executeUpdate("ALTER TABLE synonym ADD CONSTRAINT synonym_schema_fkey FOREIGN KEY (schema_id) REFERENCES \"schema\"(id)");
        stmt.executeUpdate("CREATE INDEX synonym_schema_idx ON synonym (schema_id)");
		stmt.close();
	}

}
