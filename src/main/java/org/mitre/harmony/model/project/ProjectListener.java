// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.model.project;


/**
 * Interface used by all Harmony project listeners
 * @author CWOLF
 */
public interface ProjectListener
{
	/** Indicates that the project has been modified */
	public void projectModified();

	/** Indicates that a schema has been added */
	public void schemaAdded(Integer schemaID);
	
	/** Indicates that a schema has been removed */
	public void schemaRemoved(Integer schemaID);
	
	/** Indicates that a schema model has been modified */
	public void schemaModelModified(Integer schemaID);
}
