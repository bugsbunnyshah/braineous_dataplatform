// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.view.schemaTree;

/**
 * Interface used by all schema tree listeners
 * @author CWOLF
 */
public interface SchemaTreeListener
{
	/** Indicates a change to the schema structure */
	public void schemaStructureModified(SchemaTree tree);

	/** Indicates a change to the schema display */
	public void schemaDisplayModified(SchemaTree tree);
}
