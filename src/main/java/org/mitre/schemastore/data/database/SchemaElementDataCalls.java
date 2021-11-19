// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.Synonym;

/**
 * Handles schema element data calls in the database
 * @author CWOLF
 */
public class SchemaElementDataCalls extends AbstractDataCalls
{	
	/** Constructs the data call class */
	SchemaElementDataCalls(DatabaseConnection connection) { super(connection); }

	/** Retrieves the default domain values from the repository */
	public ArrayList<Domain> getDefaultDomains()
	{
		ArrayList<Domain> defaultDomains = new ArrayList<Domain>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,name,description FROM \"domain\" WHERE id<0");
			while(rs.next())
				defaultDomains.add(new Domain(rs.getInt("id"),rs.getString("name"),rs.getString("description"),null));
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaElementDataCalls:getDefaultDomains: "+e.getMessage()); }
		return defaultDomains;
	}

	/** Retrieves the default domain value count from the repository */
	public Integer getDefaultDomainCount()
	{
		Integer defaultDomainCount = 0;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS count FROM \"domain\" WHERE id<0");
			if(rs.next()) defaultDomainCount = rs.getInt("count");
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaElementDataCalls:getDefaultDomainCount: "+e.getMessage()); }
		return defaultDomainCount;
	}

	/** Retrieves the list of base elements for the specified schema in the repository */
	public ArrayList<SchemaElement> getBaseElements(Integer schemaID)
	{
		ArrayList<SchemaElement> baseElements = new ArrayList<SchemaElement>();
		try {
			Statement stmt = connection.getStatement();

			// Gets the schema entities
			ResultSet rs = stmt.executeQuery("SELECT id,name,description FROM entity WHERE schema_id="+schemaID);
			while(rs.next())
				baseElements.add(new Entity(rs.getInt("id"),rs.getString("name"),rs.getString("description"),schemaID));

			// Gets the schema attributes
			rs = stmt.executeQuery("SELECT id,name,description,entity_id,domain_id,\"min\",\"max\",\"key\" FROM attribute WHERE schema_id="+schemaID);
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer entityID = rs.getInt("entity_id");
				Integer domainID = rs.getInt("domain_id");
				Integer min = rs.getString("min")==null?null:rs.getInt("min");
				Integer max = rs.getString("max")==null?null:rs.getInt("max");
				boolean key = rs.getString("key").equals("t");

				baseElements.add(new Attribute(id,name,description,entityID,domainID,min,max,key,schemaID));
			}

			// Gets the schema domains
			rs = stmt.executeQuery("SELECT id,name,description FROM \"domain\" WHERE schema_id="+schemaID);
			while(rs.next())
				baseElements.add(new Domain(rs.getInt("id"),rs.getString("name"),rs.getString("description"),schemaID));

			// Gets the schema domain values
			rs = stmt.executeQuery("SELECT id,value,description,domain_id FROM domainvalue WHERE schema_id="+schemaID);
			while(rs.next())
				baseElements.add(new DomainValue(rs.getInt("id"),rs.getString("value"),rs.getString("description"),rs.getInt("domain_id"),schemaID));

			// Gets the schema relationships
			rs = stmt.executeQuery("SELECT id,name,description,left_id,left_min,left_max,right_id,right_min,right_max FROM relationship WHERE schema_id="+schemaID);
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer leftID = rs.getInt("left_id");
				Integer leftMin = rs.getString("left_min")==null?null:rs.getInt("left_min");
				Integer leftMax = rs.getString("left_max")==null?null:rs.getInt("left_max");
				Integer rightID = rs.getInt("right_id");
				Integer rightMin = rs.getString("right_min")==null?null:rs.getInt("right_min");
				Integer rightMax = rs.getString("right_max")==null?null:rs.getInt("right_max");
				baseElements.add(new Relationship(id,name,description,leftID,leftMin,leftMax,rightID,rightMin,rightMax,schemaID));
			}

			// Gets the schema containment relationships
			rs = stmt.executeQuery("SELECT id,name,description,parent_id,child_id,\"min\",\"max\" FROM containment WHERE schema_id="+schemaID);
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer parentID = rs.getInt("parent_id");
				Integer childID = rs.getInt("child_id");
				Integer min = rs.getString("min")==null?null:rs.getInt("min");
				Integer max = rs.getString("max")==null?null:rs.getInt("max");
				baseElements.add(new Containment(id,name,description,parentID,childID,min,max,schemaID));
			}

			// Gets the schema subset relationships
			rs = stmt.executeQuery("SELECT id,parent_id,child_id FROM subtype WHERE schema_id="+schemaID);
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				Integer parentID = rs.getInt("parent_id");
				Integer childID = rs.getInt("child_id");
				baseElements.add(new Subtype(id,parentID,childID,schemaID));
			}

			// Gets the schema synonyms
			rs = stmt.executeQuery("SELECT id,name,description,element_id FROM synonym WHERE schema_id="+schemaID);
			while(rs.next())
				baseElements.add(new Synonym(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getInt("element_id"),schemaID));
			
			// Gets the schema aliases
			rs = stmt.executeQuery("SELECT id,name,element_id FROM alias WHERE schema_id="+schemaID);
			while(rs.next())
				baseElements.add(new Alias(rs.getInt("id"),rs.getString("name"),rs.getInt("element_id"),schemaID));

			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaElementDataCalls:getBaseElements: "+e.getMessage()); }
		return baseElements;
	}

	/** Retrieves the requested schema element from the repository */
	public SchemaElement getSchemaElement(Integer schemaElementID)
	{
		SchemaElement schemaElement = null;
		try {
			Statement stmt = connection.getStatement();

			// Determine the base and type
			Integer base = null;
			String type = "";
			ResultSet rs = stmt.executeQuery("SELECT schema_id, \"type\" FROM schema_elements WHERE id="+schemaElementID);
			if(rs.next())
				{ base = rs.getInt("schema_id"); type = rs.getString("type").trim(); }

			// Gets the specified entity
			if(type.equals("entity"))
			{
				rs = stmt.executeQuery("SELECT id,name,description FROM entity WHERE id="+schemaElementID);
				rs.next();
				schemaElement = new Entity(rs.getInt("id"),rs.getString("name"),rs.getString("description"),base);
			}

			// Gets the specified attribute
			else if(type.equals("attribute"))
			{
				rs = stmt.executeQuery("SELECT id,name,description,entity_id,domain_id,\"min\",\"max\",\"key\" FROM attribute WHERE id="+schemaElementID);
				rs.next();
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer entityID = rs.getInt("entity_id");
				Integer domainID = rs.getInt("domain_id");
				Integer min = rs.getString("min")==null?null:rs.getInt("min");
				Integer max = rs.getString("max")==null?null:rs.getInt("max");
				boolean key = rs.getString("key").equals("t");
				schemaElement = new Attribute(id,name,description,entityID,domainID,min,max,key,base);
			}

			// Gets the specified domain
			else if(type.equals("domain"))
			{
				rs = stmt.executeQuery("SELECT id,name,description FROM \"domain\" WHERE id="+schemaElementID);
				rs.next();
				schemaElement = new Domain(rs.getInt("id"),rs.getString("name"),rs.getString("description"),base);
			}

			// Gets the specified domain value
			else if(type.equals("domainvalue"))
			{
				rs = stmt.executeQuery("SELECT id,value,description,domain_id FROM domainvalue WHERE id="+schemaElementID);
				rs.next();
				schemaElement = new DomainValue(rs.getInt("id"),rs.getString("value"),rs.getString("description"),rs.getInt("domain_id"),base);
			}

			// Gets the specified relationship
			else if(type.equals("relationship"))
			{
				rs = stmt.executeQuery("SELECT id,name,description,left_id,left_min,left_max,right_id,right_min,right_max FROM relationship WHERE id="+schemaElementID);
				rs.next();
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer leftID = rs.getInt("left_id");
				Integer leftMin = rs.getString("left_min")==null?null:rs.getInt("left_min");
				Integer leftMax = rs.getString("left_max")==null?null:rs.getInt("left_max");
				Integer rightID = rs.getInt("right_id");
				Integer rightMin = rs.getString("right_min")==null?null:rs.getInt("right_min");
				Integer rightMax = rs.getString("right_max")==null?null:rs.getInt("right_max");
				schemaElement = new Relationship(id,name,description,leftID,leftMin,leftMax,rightID,rightMin,rightMax,base);
			}

			// Gets the specified containment relationship
			else if(type.equals("containment"))
			{
				rs = stmt.executeQuery("SELECT id,name,description,parent_id,child_id,\"min\",\"max\" FROM containment WHERE id="+schemaElementID);
				rs.next();
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer parentID = rs.getInt("parent_id");
				Integer childID = rs.getInt("child_id");
				Integer min = rs.getString("min")==null?null:rs.getInt("min");
				Integer max = rs.getString("max")==null?null:rs.getInt("max");
				schemaElement = new Containment(id,name,description,parentID,childID,min,max,base);
			}

			// Gets the specified subset relationship
			else if(type.equals("subtype"))
			{
				rs = stmt.executeQuery("SELECT id,parent_id,child_id FROM subtype WHERE id="+schemaElementID);
				rs.next();
				Integer id = rs.getInt("id");
				Integer parentID = rs.getInt("parent_id");
				Integer childID = rs.getInt("child_id");
				schemaElement = new Subtype(id,parentID,childID,base);
			}

			// Gets the specified synonym
			else if(type.equals("synonym"))
			{
				rs = stmt.executeQuery("SELECT id,name,description,element_id FROM synonym WHERE id="+schemaElementID);
				rs.next();
				schemaElement = new Synonym(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getInt("element_id"),base);
			}
			
			// Gets the specified alias
			else if(type.equals("alias"))
			{
				rs = stmt.executeQuery("SELECT id,name,element_id FROM alias WHERE id="+schemaElementID);
				rs.next();
				schemaElement = new Alias(rs.getInt("id"),rs.getString("name"),rs.getInt("element_id"),base);
			}

			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaElementDataCalls:getSchemaElement: "+e.getMessage()); }
		return schemaElement;
	}

	/** Adds a schema element to the specified schema */
	private void insertSchemaElement(Statement stmt, SchemaElement schemaElement) throws SQLException
	{
		// Retrieve the schema element name, description, and base ID
		Integer id = schemaElement.getId();
		String name = scrub(schemaElement.getName(),100);
		String description = scrub(schemaElement.getDescription(),4096);
		Integer baseID = schemaElement.getBase();

		// Inserts an entity
		if(schemaElement instanceof Entity)
			stmt.addBatch("INSERT INTO entity(id,name,description,schema_id) VALUES("+id+",'"+name+"','"+description+"',"+baseID+")");

		// Inserts an attribute
		if(schemaElement instanceof Attribute)
		{
			Attribute attribute = (Attribute)schemaElement;
			stmt.addBatch("INSERT INTO attribute(id,name,description,entity_id,domain_id,\"min\",\"max\",\"key\",schema_id) VALUES("+id+",'"+name+"','"+description+"',"+attribute.getEntityID()+","+attribute.getDomainID()+","+attribute.getMin()+","+attribute.getMax()+",'"+(attribute.isKey()?"t":"f")+"',"+baseID+")");
		}

		// Inserts a domain
		if(schemaElement instanceof Domain)
			stmt.addBatch("INSERT INTO \"domain\"(id,name,description,schema_id) VALUES("+id+",'"+name+"','"+description+"',"+baseID+")");

		// Inserts a domain value
		if(schemaElement instanceof DomainValue)
		{
			DomainValue domainValue = (DomainValue)schemaElement;
			stmt.addBatch("INSERT INTO domainvalue(id,value,description,domain_id,schema_id) VALUES("+id+",'"+name+"','"+description+"',"+domainValue.getDomainID()+","+baseID+")");
		}

		// Inserts a relationship
		if(schemaElement instanceof Relationship)
		{
			Relationship relationship = (Relationship)schemaElement;
			stmt.addBatch("INSERT INTO relationship(id,name,description,left_id,left_min,left_max,right_id,right_min,right_max,schema_id) VALUES("+id+",'"+name+"','"+description+"',"+relationship.getLeftID()+","+relationship.getLeftMin()+","+relationship.getLeftMax()+","+relationship.getRightID()+","+relationship.getRightMin()+","+relationship.getRightMax()+","+baseID+")");
		}

		// Inserts a containment relationship
		if(schemaElement instanceof Containment)
		{
			Containment containment = (Containment)schemaElement;
			stmt.addBatch("INSERT INTO containment(id,name,description,parent_id,child_id,\"min\",\"max\",schema_id) VALUES("+id+",'"+name+"','"+description+"',"+containment.getParentID()+","+containment.getChildID()+","+containment.getMin()+","+containment.getMax()+","+baseID+")");
		}

		// Inserts a subset relationship
		if(schemaElement instanceof Subtype)
		{
			Subtype subtype = (Subtype)schemaElement;
			stmt.addBatch("INSERT INTO subtype(id,parent_id,child_id,schema_id) VALUES("+id+","+subtype.getParentID()+","+subtype.getChildID()+","+baseID+")");
		}

		// Inserts a synonym
		if(schemaElement instanceof Synonym)
		{
			Synonym synonym = (Synonym)schemaElement;
			stmt.addBatch("INSERT INTO synonym(id,name,description,element_id,schema_id) VALUES("+id+",'"+name+"','"+description+"',"+synonym.getElementID()+","+baseID+")");
		}

		// Inserts an alias
		if(schemaElement instanceof Alias)
		{
			Alias alias = (Alias)schemaElement;
			stmt.addBatch("INSERT INTO alias(id,name,element_id,schema_id) VALUES("+id+",'"+name+"',"+alias.getElementID()+","+baseID+")");
		}
	}

	/** Adds a schema element to the specified schema */
	public Integer addSchemaElement(SchemaElement schemaElement)
	{
		Integer schemaElementID = 0;
		try {
			Statement stmt = connection.getStatement();
			schemaElementID = getUniversalIDs(1);
			schemaElement.setId(schemaElementID);
			insertSchemaElement(stmt,schemaElement);
			stmt.executeBatch();
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			schemaElementID = 0;
			System.out.println("(E) SchemaElementDataCalls:addSchemaElement: "+e.getMessage());
		}
		return schemaElementID;
	}

	/** Adds the schema elements to the specified schema */
	public boolean addSchemaElements(ArrayList<SchemaElement> schemaElements)
	{
		try {
			Statement stmt = connection.getStatement();
			for(int i=0; i<schemaElements.size(); i++)
			{
				insertSchemaElement(stmt,schemaElements.get(i));
				if(i%1000==999) stmt.executeBatch();
			}
			stmt.executeBatch();
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaElementDataCalls:addSchemaElements: "+e.getMessage());
			return false;
		}
		return true;
	}

	/** Updates the specified schema elements */
	private void updateSchemaElement(Statement stmt, SchemaElement schemaElement) throws SQLException
	{
		// Retrieve the schema element name, description, and base ID
		String name = scrub(schemaElement.getName(),100);
		String description = scrub(schemaElement.getDescription(),4096);

		// Updates an entity
		if(schemaElement instanceof Entity)
		{
			Entity entity = (Entity)schemaElement;
			stmt.addBatch("UPDATE entity SET name='"+name+"', description='"+description+"' WHERE id="+entity.getId());
		}

		// Updates an attribute
		if(schemaElement instanceof Attribute)
		{
			Attribute attribute = (Attribute)schemaElement;
			stmt.addBatch("UPDATE attribute SET name='"+name+"', description='"+description+"', entity_id="+attribute.getEntityID()+", domain_id="+attribute.getDomainID()+", min="+attribute.getMin()+", max="+attribute.getMax()+" WHERE id="+attribute.getId());
		}

		// Updates a domain
		if(schemaElement instanceof Domain)
		{
			Domain domain = (Domain)schemaElement;
			stmt.addBatch("UPDATE \"domain\" SET name='"+name+"', description='"+description+"' WHERE id="+domain.getId());
		}

		// Updates an domain value
		if(schemaElement instanceof DomainValue)
		{
			DomainValue domainValue = (DomainValue)schemaElement;
			stmt.addBatch("UPDATE domainvalue SET value='"+name+"', description='"+description+"', domain_id="+domainValue.getDomainID()+" WHERE id="+domainValue.getId());
		}

		// Updates a relationship
		if(schemaElement instanceof Relationship)
		{
			Relationship relationship = (Relationship)schemaElement;
			stmt.addBatch("UPDATE relationship SET name='"+name+"', description='"+description+"', left_id="+relationship.getLeftID()+", left_min="+relationship.getLeftMin()+", left_max="+relationship.getLeftMax()+", right_id="+relationship.getRightID()+", right_min="+relationship.getRightMin()+", right_max="+relationship.getRightMax()+" WHERE id="+relationship.getId());
		}

		// Updates a containment relationship
		if(schemaElement instanceof Containment)
		{
			Containment containment = (Containment)schemaElement;
			stmt.addBatch("UPDATE containment SET name='"+name+"', description='"+description+"' parent_id="+containment.getParentID()+", child_id="+containment.getChildID()+", \"min\"="+containment.getMin()+", \"max\"="+containment.getMax()+" WHERE id="+containment.getId());
		}

		// Updates a subset relationship
		if(schemaElement instanceof Subtype)
		{
			Subtype subtype = (Subtype)schemaElement;
			stmt.addBatch("UPDATE subtype SET parent_id="+name+", child_id="+subtype.getChildID()+" WHERE id="+subtype.getId());
		}

		// Updates a synonym
		if(schemaElement instanceof Synonym)
		{
			Synonym synonym = (Synonym)schemaElement;
			stmt.addBatch("UPDATE synonym SET name='"+name+"', description='"+description+"', element_id='"+synonym.getElementID()+"' WHERE id="+synonym.getId());
		}

		// Updates an alias
		if(schemaElement instanceof Alias)
		{
			Alias alias = (Alias)schemaElement;
			stmt.addBatch("UPDATE alias SET name='"+name+"', element_id='"+alias.getElementID()+"' WHERE id="+alias.getId());
		}
	}

	/** Updates the schema elements to the specified schema */
	public boolean updateSchemaElements(ArrayList<SchemaElement> schemaElements)
	{
		try {
			Statement stmt = connection.getStatement();
			for(int i=0; i<schemaElements.size(); i++)
			{
				updateSchemaElement(stmt,schemaElements.get(i));
				if(i%1000==999) stmt.executeBatch();
			}
			stmt.executeBatch();
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaElementDataCalls:updateSchemaElements: "+e.getMessage());
			return false;
		}
		return true;
	}


	/** Deletes the specified schema element */
	public boolean deleteSchemaElement(int schemaElementID)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();

			// Determine the element type
			String type = null;
			ResultSet rs = stmt.executeQuery("SELECT \"type\" FROM schema_elements WHERE id="+schemaElementID);
			if(rs.next()) type = rs.getString("type");
			if(type.equals("domain")) type = "\"domain\"";

			// Delete the element
			stmt.executeUpdate("DELETE FROM "+type+" WHERE id="+schemaElementID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) SchemaElementDataCalls:deleteSchemaElement: "+e.getMessage());
			return false;
		}
		return success;
	}

	/** Retrieves the list of elements using the specified keyword in the repository */
	public ArrayList<SchemaElement> getSchemaElementsForKeyword(String keyword, ArrayList<Integer> schemaIDs)
	{
		ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>();
		try {
			Statement stmt = connection.getStatement();
			keyword = new String(' ' + keyword + ' ').replaceAll("^ \\*","").replaceAll("\\* $","").replaceAll("\\*","%");
			String nameFilter = "lower(' '||name||' ') LIKE '%"+keyword.toLowerCase()+"%'";
			String valueFilter = "lower(' '||value||' ') LIKE '%"+keyword.toLowerCase()+"%'";
			String descFilter = "lower(' '||description||' ') LIKE '%"+keyword.toLowerCase()+"%'";

			// Generate the base filter
			String baseFilter;
			if(schemaIDs.size()>0)
			{
				baseFilter = "schema_id IN (";
				for(Integer schemaID : schemaIDs)
					baseFilter += schemaID + ",";
				baseFilter = substring(baseFilter, baseFilter.length()-1) + ")";
			}
			else baseFilter = "1=1";

			// Gets the schema entities
			ResultSet rs = stmt.executeQuery("SELECT id,name,description,schema_id FROM entity WHERE "+baseFilter+" AND (" + nameFilter + " OR " + descFilter + ")");
			while(rs.next())
				elements.add(new Entity(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getInt("schema_id")));

			// Gets the schema attributes
			rs = stmt.executeQuery("SELECT id,name,description,entity_id,domain_id,\"min\",\"max\",\"key\",schema_id FROM attribute WHERE "+baseFilter+" AND (" + nameFilter + " OR " + descFilter + ")");
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer entityID = rs.getInt("entity_id");
				Integer domainID = rs.getInt("domain_id");
				Integer min = rs.getString("min")==null?null:rs.getInt("min");
				Integer max = rs.getString("max")==null?null:rs.getInt("max");
				Integer schemaID = rs.getInt("schema_id");
				boolean key = rs.getString("key").equals("t");
				elements.add(new Attribute(id,name,description,entityID,domainID,min,max,key,schemaID));
			}

			// Gets the schema domains
			rs = stmt.executeQuery("SELECT id,name,description,schema_id FROM \"domain\" WHERE "+baseFilter+" AND (" + nameFilter + " OR " + descFilter + ")");
			while(rs.next())
				elements.add(new Domain(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getInt("schema_id")));

			// Gets the schema domain values
			rs = stmt.executeQuery("SELECT id,value,description,domain_id,schema_id FROM domainvalue WHERE "+baseFilter+" AND (" + valueFilter + " OR " + descFilter + ")");
			while(rs.next())
				elements.add(new DomainValue(rs.getInt("id"),rs.getString("value"),rs.getString("description"),rs.getInt("domain_id"),rs.getInt("schema_id")));

			// Gets the schema relationships
			rs = stmt.executeQuery("SELECT id,name,description,left_id,left_min,left_max,right_id,right_min,right_max,schema_id FROM relationship WHERE "+baseFilter+" AND " + nameFilter);
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer leftID = rs.getInt("left_id");
				Integer leftMin = rs.getString("left_min")==null?null:rs.getInt("left_min");
				Integer leftMax = rs.getString("left_max")==null?null:rs.getInt("left_max");
				Integer rightID = rs.getInt("right_id");
				Integer rightMin = rs.getString("right_min")==null?null:rs.getInt("right_min");
				Integer rightMax = rs.getString("right_max")==null?null:rs.getInt("right_max");
				Integer schemaID = rs.getInt("schema_id");
				elements.add(new Relationship(id,name,description,leftID,leftMin,leftMax,rightID,rightMin,rightMax,schemaID));
			}

			// Gets the schema containment relationships
			rs = stmt.executeQuery("SELECT id,name,description,parent_id,child_id,\"min\",\"max\",schema_id FROM containment WHERE "+baseFilter+" AND (" + nameFilter + " OR " + descFilter + ")");
			while(rs.next())
			{
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				Integer parentID = rs.getInt("parent_id");
				Integer childID = rs.getInt("child_id");
				Integer min = rs.getString("min")==null?null:rs.getInt("min");
				Integer max = rs.getString("max")==null?null:rs.getInt("max");
				Integer schemaID = rs.getInt("schema_id");
				elements.add(new Containment(id,name,description,parentID,childID,min,max,schemaID));
			}

			// Gets the schema synonyms
			rs = stmt.executeQuery("SELECT id,name,description,element_id,schema_id FROM synonym WHERE "+baseFilter+" AND " + nameFilter);
			while(rs.next())
				elements.add(new Synonym(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getInt("element_id"),rs.getInt("schema_id")));
			
			// Gets the schema aliases
			rs = stmt.executeQuery("SELECT id,name,alias.element_id,schema_id FROM alias WHERE "+baseFilter+" AND " + nameFilter);
			while(rs.next())
				elements.add(new Alias(rs.getInt("id"),rs.getString("name"),rs.getInt("element_id"),rs.getInt("schema_id")));

			stmt.close();
		} catch(SQLException e) { System.out.println("(E) SchemaElementDataCalls:getSchemaElementsForKeyword: "+e.getMessage()); }
		return elements;
	}
}