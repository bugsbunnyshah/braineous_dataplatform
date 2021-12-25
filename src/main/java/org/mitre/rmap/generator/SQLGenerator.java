// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.generator;

import org.mitre.rmap.model.exports.Export;
import org.mitre.schemastore.model.*;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SQLGenerator {
	public static final String TABLE_CHAR = "T";
	public static final String VARIABLE_CHAR ="V";
	public static final String SKOLEM_TABLE_CHAR = "SK";
	public static final String DELIM = "\"";

	private ArrayList<String> targetExportTypes;
	private Object exportType;	// contains the object that has our Export implementation

	public ArrayList<String> getExportTypes() {
		if (targetExportTypes == null) {
			// build the list
			// TODO: make the list come out of the database
			targetExportTypes = new ArrayList<String>();
			targetExportTypes.add("Derby");
			targetExportTypes.add("Postgres");
		}
		return targetExportTypes;
	}

	public Boolean checkForErrors(ArrayList<Dependency> dependencies) throws SQLGeneratorException {
		for (Dependency dependency : dependencies) {
			for (MappingCell cell : dependency.getCoveredCorrespondences()) {
				SchemaElement sourceElement = dependency.getSourceLogicalRelation().getMappingSchemaInfo().getElement(cell.getElementInputIDs()[0]);
				SchemaElement targetElement = dependency.getTargetLogicalRelation().getMappingSchemaInfo().getElement(cell.getOutput());

				if (sourceElement == null) {
					throw new SQLGeneratorException("Correspondence references NULL source attribute.");
				}
				if (!(sourceElement instanceof Attribute)) {
					throw new SQLGeneratorException("Dependency source element " + sourceElement.getName() + " is not an attribute.");
				}
				if (targetElement == null) {
					throw new SQLGeneratorException("Correspondence references NULL target attribute.");
				}
				if (!(targetElement instanceof Attribute)) {
					throw new SQLGeneratorException("Dependency target element " + targetElement.getName() + " is not an attribute.");
				}
				if (sourceElement instanceof Attribute && !((Attribute)sourceElement).isKey() && targetElement instanceof Attribute && ((Attribute)targetElement).isKey()) {
					throw new SQLGeneratorException("Dependency target element " + targetElement.getName() + " is key attribute and can only be populated by key attributes.");
				}
				if (cell.getScore() != 1.0) {
					throw new SQLGeneratorException("Dependency between " + sourceElement.getName() + " and " + targetElement.getName() + " must be validated.");
				}
			}
		}
		return true;
	}

	/**
	 * PART OF INTERFACE TO GUI -- takes selected dependences --> final SQL Script
	 *
	 * generateFinalSQLScript: Generates the "final" SQL script
	 * @param dependStatementBlocks each statement block has type ArrayList<String>
	 * @return final executable SQL script (each line as separate string)
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> generate(ArrayList<Dependency> dependencies, String targetDB) throws SQLGeneratorException {
		ArrayList<String> value = new ArrayList<String>();

		// create a new object from the targetDB
		try {
			exportType = Class.forName("org.org.mitre.rmap.model.exports." + targetDB).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLGeneratorException("Could not create an instance of the " + targetDB + " class.");
		}

		/** Perform topological sort of entities */
		ArrayList<Entity> toplogicalSortedEntities = topologicalSort(dependencies);

		// should add any special declarations to the top of the generated sql
		value.add((String)((Export)exportType).getOpeningSQL());

		ArrayList<String> finalSkolemStatements = new ArrayList<String>();
		ArrayList<String> finalSelectStatements = new ArrayList<String>();
		ArrayList<String> finalCleanupStatements = new ArrayList<String>();

		HashMap<Integer, String> selectStatementBySSID = new HashMap<Integer, String>();

		for (Dependency dependency : dependencies) {
			// create SQL statement block for each dependency
			Object[] dependencyStatements  = generateDependencySQLScript(dependency, targetDB);
			finalSkolemStatements.addAll((ArrayList<String>)dependencyStatements[0]);
			finalCleanupStatements.addAll((ArrayList<String>)dependencyStatements[2]);

			for (Integer mapID : ((HashMap<Integer, String>)dependencyStatements[1]).keySet()) {
				String currStmt = ((HashMap<Integer, String>)dependencyStatements[1]).get(mapID);
				Integer ssID = dependency.getTargetLogicalRelation().getIDmappings_LR_to_SS().get(mapID);
				String unionStmt = selectStatementBySSID.get(ssID);

				if (unionStmt == null) {
					unionStmt = currStmt.replace(";","");
				} else {
					currStmt = currStmt.replace(";", "");
					Pattern myPattern = Pattern.compile("SELECT DISTINCT");
					String[] strArray = myPattern.split(currStmt);
					unionStmt += new String(" UNION SELECT DISTINCT") + strArray[1];
				}

				selectStatementBySSID.put(ssID, unionStmt);
			}
		} // end for each dependency block

		for (Entity entity : toplogicalSortedEntities) {
			if (selectStatementBySSID.get(entity.getId()) != null) {
				finalSelectStatements.add(selectStatementBySSID.get(entity.getId()) + ";");
			}
		}

		// should add any special cleaning statements to the bottom of the generated sql
		finalCleanupStatements.add((String)((Export)exportType).getClosingSQL());

		value.addAll(finalSkolemStatements);
		value.addAll(finalSelectStatements);
		value.addAll(finalCleanupStatements);
		return value;
	}

	// generates SQL to generate the Skolem table
	private Object[] generateDependencySQLScript(Dependency dependency, String targetDB) throws SQLGeneratorException {
		Object[] value = new Object[3];
		ArrayList<String> cleanup = new ArrayList<String>();
		HashMap<Integer, Boolean> needToGen = new HashMap<Integer, Boolean>();
		HashMap<Integer, Boolean> needToGenSkolem = new HashMap<Integer, Boolean>();
		HashMap<Integer, String> statements = generateStatements(dependency, needToGen, needToGenSkolem);
		ArrayList<String> skolemStatements = createSkolemTables(dependency, cleanup, needToGenSkolem, targetDB);

		value[0] = skolemStatements;
		value[1] = statements;
		value[2] = cleanup;
		return value;
	}

	private ArrayList<Entity> topologicalSort (ArrayList<Dependency> dependencies) throws SQLGeneratorException {
		HashMap<Integer,Entity> entitiesBySSID = new HashMap<Integer,Entity>();
		ArrayList<Relationship> relsWithSSID = new ArrayList<Relationship>();

		for (Dependency depend : dependencies) {
			// build the UNION of Entities from SchemaStore schemaInfos across all dependencies
			for (Entity entity : depend.getTargetLogicalRelation().getEntitySet()) {
				entitiesBySSID.put(entity.getId(),entity);
			}

			// build the UNION of all relationships across the schemaInfo
			// NOTE:  need to copy the relationship so LEFT and RIGHT in SSID-space are consistent
			for (SchemaElement se : depend.getTargetLogicalRelation().getMappingSchemaInfo().getElements(Relationship.class)){
				Relationship rel = ((Relationship)se).copy();
				rel.setLeftID(depend.getTargetLogicalRelation().getIDmappings_LR_to_SS().get(rel.getLeftID()));
				rel.setRightID(depend.getTargetLogicalRelation().getIDmappings_LR_to_SS().get(rel.getRightID()));
				relsWithSSID.add(rel);
			}
		}

		boolean proceed = true;
		ArrayList<Entity> value = new ArrayList<Entity>();

		while (proceed) {
			 // find all entities e without rel r s.t. r.rightID == e.id
			ArrayList<Entity> leafEntities = new ArrayList<Entity>();
			ArrayList<Integer> entityIDList = new ArrayList<Integer>(entitiesBySSID.keySet());
			for (Integer entityID : entityIDList){
				boolean isLeaf = true;
				for (Relationship rel : relsWithSSID) {
					if (rel.getLeftID().equals(entityID)) {
						isLeaf = false;
					}
				}
				if (isLeaf) {
					leafEntities.add(entitiesBySSID.get(entityID));
					entitiesBySSID.remove(entityID);
				}
			}
			proceed = (leafEntities.size() > 0);
			value.addAll(leafEntities);

			// for each relationship rel, check that rel.rightID not removed from entity set
			ArrayList<Relationship> relsLeft = new ArrayList<Relationship>();
			for (Relationship rel : relsWithSSID) {
				if (entitiesBySSID.get(rel.getRightID()) != null) {
					relsLeft.add(rel);
				}
			}

			relsWithSSID = relsLeft;
		}
		return value;
	}

	/**
	 * // generates SQL to generate the Skolem table
	   // 1) generates a sequence
	   // 2) uses sequence to assign key values to a field called "skid"
	 * @param dependency
	 * @param cleanup (modified by function) commands to "cleanup" temporary data structures created by complete script
	 * @param needToGenSkolem
	 * @return
	 */
	private ArrayList<String> createSkolemTables(Dependency dependency, ArrayList<String> cleanup, HashMap<Integer, Boolean> needToGenSkolem, String targetDB) throws SQLGeneratorException {
		ArrayList<String> retVal = new ArrayList<String>();
		HierarchicalSchemaInfo sourceSchemaInfo = dependency.getSourceLogicalRelation().getMappingSchemaInfo();

		// check for each source entity whether we need to create skolem table or not
		for (Integer entityID : needToGenSkolem.keySet()) {
			if (needToGenSkolem.get(entityID) != null && needToGenSkolem.get(entityID) == true) {
				// generate a skolem table that has value for EVERY covered attribute
				String skolemTableName = SQLGenerator.SKOLEM_TABLE_CHAR  + entityID;
				String createTable = new String("CREATE TABLE " + DELIM + skolemTableName + DELIM + " ( ");

				// add on special parameters for creating a table
				createTable += (String)((Export)exportType).getCreateTableOptions();

				// Generate CREATE TABLE statement for skolem table
				int index = 0;
				for (int i = 0; i < dependency.getCoveredCorrespondences().size(); i++) {
					MappingCell cell = dependency.getCoveredCorrespondences().get(i);

					// get the domain type for attribute
					for (Integer inputId : cell.getElementInputIDs()) {
						String domainName = "String";
						if (sourceSchemaInfo.getElement(inputId) instanceof Relationship) {
							Integer entityId = ((Relationship)sourceSchemaInfo.getElement(inputId)).getRightID();
							domainName = sourceSchemaInfo.getElement(getKey(entityId, sourceSchemaInfo).getDomainID()).getName();
						}
						else if (sourceSchemaInfo.getElement(inputId) instanceof Attribute) {
							domainName = sourceSchemaInfo.getElement(((Attribute)sourceSchemaInfo.getElement(inputId)).getDomainID()).getName();
						}
						else {
							throw new SQLGeneratorException("No domain assigned skolem attribute.");
						}
						createTable += DELIM + SQLGenerator.VARIABLE_CHAR + index + DELIM + " " + domainName;
						index++;
					}
					if (i < dependency.getCoveredCorrespondences().size()-1) { createTable += ", "; }

				}
				createTable += " );";
				retVal.add(createTable);
				cleanup.add(new String("DROP TABLE " + DELIM + skolemTableName + DELIM +";"));

				String insertInto= "INSERT INTO " + DELIM + skolemTableName + DELIM + " ( ";
				for (int i=0 ; i < dependency.getCoveredCorrespondences().size(); i++) {
					insertInto += DELIM + SQLGenerator.VARIABLE_CHAR + i + DELIM;
					if (i < dependency.getCoveredCorrespondences().size() - 1) { insertInto += ", "; }
				}
				insertInto += " ) ";

				// create select distinct statement to create skolem table
				insertInto += " SELECT DISTINCT ";

				// Generate CREATE TABLE statement for skolem table

				for (int i=0 ; i < dependency.getCoveredCorrespondences().size(); i++) {
					MappingCell cell = dependency.getCoveredCorrespondences().get(i);

					for (Integer indexId : cell.getElementInputIDs()){
						Integer pathId = 0;
						if (sourceSchemaInfo.getElement(indexId) instanceof Relationship) {
							pathId = dependency.getSourceLogicalRelation().getEntityIndicesByRel().get(dependency.getSourceLogicalRelation().getIDmappings_LR_to_SS().get(sourceSchemaInfo.getElement(indexId).getId())).get(0);
						}
						else if (sourceSchemaInfo.getElement(indexId) instanceof Attribute) {
							pathId = dependency.getSourceLogicalRelation().getPositionMappingSchemaEntitySet(sourceSchemaInfo.getEntity(indexId).getId() );
						}
						else {
							throw new SQLGeneratorException("Source is neither a relationship nor an attribute.");
						}
						String attrName = sourceSchemaInfo.getElement(indexId).getName();
						attrName = DELIM + attrName + DELIM;
						insertInto += DELIM + SQLGenerator.TABLE_CHAR + pathId + DELIM + "." + attrName;
					}
					if (i < dependency.getCoveredCorrespondences().size()-1) insertInto += ",";
				}
				insertInto += generateFromWhereLogRel(dependency.getSourceLogicalRelation());
				retVal.add(insertInto);
			}
		}
		return retVal;
	}

	// generates a collection of SQL statements (one per relation (i.e., PATH) in target logical relation)
	// where SKOLEM tables are to be used for Attribute, generate SELECT statement from appropriate Skolem table
	// to populate the Attribute
	private HashMap<Integer, String> generateStatements(Dependency dependency, HashMap<Integer, Boolean> needToGen, HashMap<Integer, Boolean> needToGenSkolem) throws SQLGeneratorException {
		HierarchicalSchemaInfo sourceSchemaGraph = dependency.getSourceLogicalRelation().getMappingSchemaInfo();
		HierarchicalSchemaInfo targetSchemaGraph = dependency.getTargetLogicalRelation().getMappingSchemaInfo();
		HashMap<Integer, String> value = new HashMap<Integer, String>();

		/** Determine which target entities require generation */
		/** PASS 1:  Determine which entities have correspondences */
		for(Entity path : dependency.getTargetLogicalRelation().getMappingSchemaEntitySet()) {
			// CASE: target entity has generated values
			for (MappingCell cell : dependency.getCoveredCorrespondences()) {
				Integer outputEntityId = null;

				if (targetSchemaGraph.getElement(cell.getOutput()) instanceof Relationship) {
					outputEntityId = ((Relationship)targetSchemaGraph.getElement(cell.getOutput())).getLeftID();
				} else if (targetSchemaGraph.getElement(cell.getOutput()) instanceof Attribute) {
					outputEntityId = targetSchemaGraph.getEntity(cell.getOutput()).getId();
				} else {
					throw new SQLGeneratorException("Target is neither a relationship nor an attribute.");
				}

				if (outputEntityId.equals(path.getId())) {
					needToGen.put(path.getId(), true);
				}
			}
		}

		/** PASS 2: Determine which entities must be generated to satisfy FK dependencies (to entities identified in Pass 1)*/
		for (SchemaElement se : targetSchemaGraph.getElements(Relationship.class)) {
			if (needToGen.get(((Relationship)se).getLeftID()) != null &&  needToGen.get(((Relationship)se).getLeftID()) == true) {
				needToGen.put(((Relationship)se).getRightID(), true);
			}
		}

		/** PASS 3: Remaining entities do not need to be generated */
		for (Entity path : dependency.getTargetLogicalRelation().getMappingSchemaEntitySet()) {
			if (needToGen.get(path.getId()) == null) {
				needToGen.put(path.getId(), false);
			}
		}

		for (Entity path : dependency.getTargetLogicalRelation().getMappingSchemaEntitySet()) {
			if (needToGen.get(path.getId()) == true) {
				String insertIntoString = "INSERT INTO " + DELIM + path.getName() + DELIM + " (";
				for (int i = 0; i < targetSchemaGraph.getChildElements(path.getId()).size(); i++) {
					String attrString = targetSchemaGraph.getChildElements(path.getId()).get(i).getName();
					attrString = DELIM + attrString + DELIM;
					insertIntoString += attrString;
					if (i < targetSchemaGraph.getChildElements(path.getId()).size() - 1) { insertIntoString += ","; }
				}
				insertIntoString += ") ";

				// Build SELECT DISTINCT string
				String selectString = new String(" SELECT DISTINCT ");
				String valueString = new String();
				for (int i = 0; i < targetSchemaGraph.getChildElements(path.getId()).size(); i++) {
					SchemaElement child = targetSchemaGraph.getChildElements(path.getId()).get(i);
					Attribute targetChild = null;
					if (child instanceof Relationship) {
						targetChild = getKey(targetSchemaGraph.getElement(((Relationship)child).getRightID()).getId(), targetSchemaGraph);
					} else {
						targetChild = (Attribute)child;
					}

					// check to see if values must be generated
					boolean isNullable = true, useSkolem = false;
					if (targetChild.isKey()) { useSkolem = true; isNullable = false; }
					if (targetChild.getMin() == null || targetChild.getMin() != 0) { isNullable = false; }

					if (isNullable == false && useSkolem == true) {
						// use SKOLEM table
						needToGenSkolem.put(targetSchemaGraph.getEntity(targetChild.getId()).getId(), true);

						valueString += " ( SELECT " + DELIM + "skid" + DELIM + " FROM " + DELIM + SQLGenerator.SKOLEM_TABLE_CHAR + targetSchemaGraph.getEntity(targetChild.getId()).getId() + DELIM + " WHERE ";

						for (int j = 0; j < dependency.getCoveredCorrespondences().size(); j++) {
							MappingCell cell = dependency.getCoveredCorrespondences().get(j);

							for (Integer inputId : cell.getElementInputIDs()){
								Integer pathId = 0;
								if (sourceSchemaGraph.getElement(inputId) instanceof Relationship) {
									pathId = dependency.getSourceLogicalRelation().getEntityIndicesByRel().get(dependency.getSourceLogicalRelation().getIDmappings_LR_to_SS().get(sourceSchemaGraph.getElement(inputId).getId())).get(0);
								} else if (sourceSchemaGraph.getElement(inputId) instanceof Attribute) {
									pathId = dependency.getSourceLogicalRelation().getPositionMappingSchemaEntitySet(sourceSchemaGraph.getEntity(inputId).getId() );
								} else {
									throw new SQLGeneratorException("Source is neither a relationship nor an attribute.");
								}

								String attrName = sourceSchemaGraph.getElement(inputId).getName();
								attrName = DELIM + attrName +DELIM;
								valueString += DELIM + SQLGenerator.VARIABLE_CHAR + j + DELIM + " = " + DELIM + SQLGenerator.TABLE_CHAR +pathId + DELIM + "." + attrName;
							}
							if (j < dependency.getCoveredCorrespondences().size() - 1) { valueString += " AND "; }
						}
						valueString += ")";
					} else if (isNullable == false && useSkolem == false) {
						valueString = "1";
					} else if (isNullable == true && useSkolem == false) {
						valueString = "null";
					} else {
						throw new SQLGeneratorException("Cannot have a nullable field and be required to generate a skolem constant.");
					}

					selectString += valueString;
					if (i < targetSchemaGraph.getChildElements(path.getId()).size() - 1) { selectString += ","; }
				} // end for

				String queryString = insertIntoString + selectString + generateFromWhereLogRel(dependency.getSourceLogicalRelation());
				value.put(path.getId(),queryString);
			} // end if (needToGen.get(path.getId()) == true){
		}
		return value;
	}

	private Attribute getKey(Integer id, HierarchicalSchemaInfo targetSchemaGraph) {
		for (SchemaElement se : targetSchemaGraph.getChildElements(id)) {
			if (se instanceof Attribute && ((Attribute)se).isKey()) {
				return (Attribute)se;
			}
		}
		return null;
	}

	/**
	 * generateFromWhereLogRel(): Generates the FROM-WHERE clause to create given Logical Relation
	 * @param relation Given logical relation
	 * @param schemaGraph the schema graph on which the logical relation is based
	 * @return String containing FROM-WHERE statement to generate logical relation
	 */
	private String generateFromWhereLogRel(LogicalRelation relation) {
		// FROM: list of paths in source schemaRelation
		String fromString = new String(" FROM ");
		for (int i = 0; i < relation.getMappingSchemaEntitySet().size(); i++) {
			String relationName = relation.getMappingSchemaEntitySet().get(i).getName();
			fromString += DELIM + relationName + DELIM + " AS " + DELIM+ SQLGenerator.TABLE_CHAR + i + DELIM ;
			if (i < relation.getEntitySet().size() - 1) { fromString += ", "; }
		}

		ArrayList<SchemaElement> relationships = relation.getMappingSchemaInfo().getElements(Relationship.class);
		String whereString = new String("");
		if (relationships.size() > 0) { whereString += " WHERE "; }
		for (int i = 0; i<relationships.size(); i++) {
			// CLAUSE: leftEntityName.relationshipName = rightEntityName.rightAttrName
			Relationship currRel = (Relationship)relationships.get(i);
			String relName = DELIM + currRel.getName() + DELIM;
			String rightAttrName = DELIM + (getKey(currRel.getRightID(),relation.getMappingSchemaInfo())).getName() + DELIM;
			Integer leftEntityIndex = relation.getEntityIndicesByRel().get(relation.getIDmappings_LR_to_SS().get(currRel.getId())).get(0);
			Integer rightEntityIndex = relation.getEntityIndicesByRel().get(relation.getIDmappings_LR_to_SS().get(currRel.getId())).get(1);
			String leftTableName = DELIM + SQLGenerator.TABLE_CHAR + leftEntityIndex + DELIM;
			String rightTableName = DELIM + SQLGenerator.TABLE_CHAR + rightEntityIndex + DELIM;

			whereString += leftTableName +  "." + relName + " = " + rightTableName  + "." + rightAttrName;
			if (i < relationships.size() - 1) { whereString += " AND "; }
		}
		return new String(fromString + whereString +";");
	}
}
