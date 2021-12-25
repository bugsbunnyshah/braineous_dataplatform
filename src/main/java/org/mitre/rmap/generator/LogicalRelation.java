// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.generator;

import org.mitre.schemastore.model.*;
import org.mitre.schemastore.model.schemaInfo.*;
import org.mitre.schemastore.model.schemaInfo.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LogicalRelation {
	static private Integer nextID = 1;
	static public Integer getNextID() { return nextID++; }

	// needs to be copied
	private HierarchicalSchemaInfo mappingSchemaInfo;
	private HashMap<Integer, ArrayList<Integer>> entityIndicesByRel;
	private ArrayList<Entity> entitySet;	 			 // set of paths followed in the chase (schemaStore schemaInfo ID space)
	private ArrayList<Entity> mappingSchemaEntitySet; // entity set  for copy in mappingSchemaInfo
	private HashMap<Integer, Integer> IDmappings_LR_to_SS;
	private HashMap<Integer, ArrayList<Integer>> IDmappings_SS_to_LR;

	public HashMap<Integer, Integer> getIDmappings_LR_to_SS() {
		return IDmappings_LR_to_SS;
	}

	public HashMap<Integer, ArrayList<Integer>> getIDmappings_SS_to_LR() {
		return IDmappings_SS_to_LR;
	}

	/**
	 * translate:  enumerate all possible translation of input array of *SchemaStore IDs* 
	 * @param inputs
	 * @return
	 */
	public MappingCellInput[] translate_SS_to_LR (MappingCellInput[] inputs)
	{
		// First make sure a transformation is possible for the input IDs
		for(MappingCellInput input : inputs)
			if(IDmappings_SS_to_LR.get(input.getElementID()) == null) return null;
		
		// Transform the input IDs
		MappingCellInput[] translatedInputs = new MappingCellInput[inputs.length];
		for(int i=0; i<inputs.length; i++)
		{
			Integer translatedID = IDmappings_SS_to_LR.get(inputs[i].getElementID()).get(0);
			translatedInputs[i] = new MappingCellInput(translatedID);
		}
		return translatedInputs;
	}
		
	public int getPositionMappingSchemaEntitySet(Integer passedID){
		for (int i = 0; i < mappingSchemaEntitySet.size() ; i++) {
			if (mappingSchemaEntitySet.get(i).getId().equals(passedID)) {
				return i;
			}
		}
		return -1;
	}
	

	/**
	 * copy(): creates a deep copy of the LogicalRelation
	 * @return the copy
	 */
	public LogicalRelation copy(){
		LogicalRelation copy = new LogicalRelation();
		HashMap<Integer, Integer> copyIDmappings_LR_to_SS = new HashMap<Integer, Integer>();
		for (Integer key : this.IDmappings_LR_to_SS.keySet()) {
			copyIDmappings_LR_to_SS.put(key, this.IDmappings_LR_to_SS.get(key));
		}

		HashMap<Integer, ArrayList<Integer>> copyEntityIndicesByRel = new HashMap<Integer, ArrayList<Integer>>();
		for (Integer key : this.entityIndicesByRel.keySet()) {
			ArrayList<Integer> idxSet = this.entityIndicesByRel.get(key);
			ArrayList<Integer> idxSetCopy = new ArrayList<Integer>();
			for (Integer id : idxSet) { idxSetCopy.add(id); }
			copyEntityIndicesByRel.put(key,idxSetCopy);
		}
		
		HashMap<Integer, ArrayList<Integer>> copyIDmappings_SS_to_LR = new HashMap<Integer, ArrayList<Integer>>();
		for (Integer key : this.IDmappings_SS_to_LR.keySet()){
			ArrayList<Integer> idSet = this.IDmappings_SS_to_LR.get(key);
			ArrayList<Integer> idSetCopy = new ArrayList<Integer>();
			for (Integer id : idSet) { idSetCopy.add(id); }
			copyIDmappings_SS_to_LR.put(key,idSetCopy);
		}

		ArrayList<Entity> copyEntitySet = new ArrayList<Entity>();
		for (Entity entity : this.entitySet) {
			copyEntitySet.add(entity.copy());
		}

		ArrayList<Entity> copyMappingSchemaEntitySet = new ArrayList<Entity>();
		for (Entity entity : this.mappingSchemaEntitySet) {
			copyMappingSchemaEntitySet.add(entity.copy());
		}

		copy = new LogicalRelation();
		copy.mappingSchemaInfo = this.mappingSchemaInfo;
		copy.mappingSchemaEntitySet = copyMappingSchemaEntitySet;
		copy.entitySet = copyEntitySet;
		copy.IDmappings_LR_to_SS = copyIDmappings_LR_to_SS;
		copy.IDmappings_SS_to_LR = copyIDmappings_SS_to_LR;
		copy.entityIndicesByRel = copyEntityIndicesByRel;

		return copy;
	} // end method copy

	public ArrayList<Entity> getEntitySet() {
		return entitySet;
	}
	
	public ArrayList<Entity> getMappingSchemaEntitySet() {
		return mappingSchemaEntitySet;
	}

	public HashMap<Integer, ArrayList<Integer>> getEntityIndicesByRel() {
		return entityIndicesByRel;
	}

	public Integer getSchemaId() {
		return mappingSchemaInfo.getSchema().getId();
	}

	public HierarchicalSchemaInfo getMappingSchemaInfo() {
		return mappingSchemaInfo;
	}

	public LogicalRelation(){
		entitySet = new ArrayList<Entity>();
		mappingSchemaEntitySet = new ArrayList<Entity>();
		entityIndicesByRel = new HashMap<Integer, ArrayList<Integer>>();
	}

	public ArrayList<Integer> getEntitySetIds(){
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for (Entity entity : entitySet) {
			retVal.add(entity.getId());
		}
		return retVal;
	}

	/**
	 * generateLogicalRelationSchemaInfo:  generates the schemaInfo for the 
	 * LogicalRelation by creating a "deep copy" of a given schemaInfo 
	 * in from the repository and modifying it to reflect the 
	 * results of the chase.
	 *
	 * @param schemaStoreSchemaInfo schemaInfo from repository
	 */
	private void generateLogicalRelationSchemaInfo(SchemaInfo schemaStoreSchemaInfo){
		IDmappings_LR_to_SS = new HashMap<Integer, Integer>();
		IDmappings_SS_to_LR = new HashMap<Integer, ArrayList<Integer>>();

		Integer[] newPathEntityIDbyPosition = new Integer[entitySet.size()];
		HashMap<Integer, Integer> domainSS_to_domainLR = new HashMap<Integer, Integer>();
		ArrayList<SchemaElement> returnSchemaElements = new ArrayList<SchemaElement>();

		// use the relational schemaInfo model to build a Relational schemaInfo for SchemaStore schemaInfo
		// that LogicalRelation schemaInfo is based on
		SchemaModel relationalModel = null, rmapModel = null;
		for (SchemaModel gm : HierarchicalSchemaInfo.getSchemaModels()){
			//if (gm.getDefaultName().equals("Relational")) { relationalModel = gm; }
			//if (gm.getDefaultName().equals("RMap - 1 to N")) { rmapModel = gm; }
		}

		if (rmapModel == null || relationalModel == null) {
			System.err.println("[E] LogicalRelation:generateLogicalRelationSchemaInfo -- relationalModel or rmapModel is null");
		}
		HierarchicalSchemaInfo relationalSchemaInfo = new HierarchicalSchemaInfo(schemaStoreSchemaInfo,relationalModel);

		Schema newSchema = new Schema(getNextID(),
				schemaStoreSchemaInfo.getSchema().getName(),
				schemaStoreSchemaInfo.getSchema().getAuthor(),
				schemaStoreSchemaInfo.getSchema().getSource(),
				schemaStoreSchemaInfo.getSchema().getType(),
				schemaStoreSchemaInfo.getSchema().getDescription(),
				false);
		
		for (Integer currIndex = 0; currIndex < entitySet.size(); currIndex++) {
			Entity pathEntity = entitySet.get(currIndex);
			Entity pathEntityCopy = pathEntity.copy();
			pathEntityCopy.setId(getNextID());
			IDmappings_LR_to_SS.put(pathEntityCopy.getId(), pathEntity.getId());

			if (IDmappings_SS_to_LR.get(pathEntity.getId()) == null) {
				IDmappings_SS_to_LR.put(pathEntity.getId(),new ArrayList<Integer>());
			}
			IDmappings_SS_to_LR.get(pathEntity.getId()).add(pathEntityCopy.getId());

			returnSchemaElements.add(pathEntityCopy);
			newPathEntityIDbyPosition[currIndex] = pathEntityCopy.getId();
			mappingSchemaEntitySet.add(pathEntityCopy);

			// create duplicate of all attributes (for path entity), and their domains (if necessary)
			for (Attribute attr : relationalSchemaInfo.getAttributes(pathEntity.getId())) {
				// create copy of attribute
				Attribute attrCopy = attr.copy();
				attrCopy.setId(getNextID());
				attrCopy.setEntityID(pathEntityCopy.getId());

				IDmappings_LR_to_SS.put(attrCopy.getId(),attr.getId());
				if (IDmappings_SS_to_LR.get(attr.getId()) == null) {
					IDmappings_SS_to_LR.put(attr.getId(),new ArrayList<Integer>());
				}
				IDmappings_SS_to_LR.get(attr.getId()).add(attrCopy.getId());

				returnSchemaElements.add(attrCopy);

				// check to see if domain has been added
				if (domainSS_to_domainLR.get(attr.getDomainID()) == null) {
					Domain attrDomain = relationalSchemaInfo.getDomainForElement(attr.getId());
					Domain attrDomainCopy = attrDomain.copy();
					attrDomainCopy.setId(getNextID());
					domainSS_to_domainLR.put(attrDomain.getId(), attrDomainCopy.getId());
					IDmappings_LR_to_SS.put(attrDomainCopy.getId(), attrDomain.getId());

					if (IDmappings_SS_to_LR.get(attrDomain.getId()) == null) {
						IDmappings_SS_to_LR.put(attrDomain.getId(),new ArrayList<Integer>());
					}
					IDmappings_SS_to_LR.get(attrDomain.getId()).add(attrDomainCopy.getId());

					returnSchemaElements.add(attrDomainCopy);
					for (DomainValue dv : relationalSchemaInfo.getDomainValuesForDomain(attrDomain.getId())) {
						DomainValue dvCopy = dv.copy();
						dvCopy.setId(getNextID());
						dvCopy.setDomainID(attrDomainCopy.getId());
						IDmappings_LR_to_SS.put(dvCopy.getId(),dv.getId());
						returnSchemaElements.add(dvCopy);
					}
				}
				attrCopy.setDomainID(domainSS_to_domainLR.get(attr.getDomainID()));
			}

			// add relationships
			for (SchemaElement se : schemaStoreSchemaInfo.getElements(Relationship.class)) {
				if (entityIndicesByRel.containsKey(se.getId())) {
					Relationship relation = (Relationship)se;
					Integer leftIndex = entityIndicesByRel.get(relation.getId()).get(0);
					Integer rightIndex = entityIndicesByRel.get(relation.getId()).get(1);

					// if currentIndex is idTriple rightPathIndex, then copy relationship
					if (relation.getRightID().equals(entitySet.get(currIndex).getId()) && rightIndex == currIndex) {
						// create copy of this relationship
						Relationship rel = (Relationship)relationalSchemaInfo.getElement(relation.getId());
						Relationship relCopy = rel.copy();
						relCopy.setId(getNextID());
						relCopy.setLeftID(newPathEntityIDbyPosition[leftIndex]);
						relCopy.setRightID(newPathEntityIDbyPosition[rightIndex]);
	
						IDmappings_LR_to_SS.put(relCopy.getId(),rel.getId());
						if (IDmappings_SS_to_LR.get(rel.getId()) == null) {
							IDmappings_SS_to_LR.put(rel.getId(),new ArrayList<Integer>());
						}
						IDmappings_SS_to_LR.get(rel.getId()).add(relCopy.getId());
						returnSchemaElements.add(relCopy);
					}
				}
			}
		} 

		// create the SchemaInfo
		this.mappingSchemaInfo = new HierarchicalSchemaInfo(new SchemaInfo(newSchema, new ArrayList<Integer>(),returnSchemaElements),rmapModel);
	}

	/**
	 * createLogicalRelations: create the set of logical relations for a given
	 * schema by applying the relational chase to each entity in given schema
	 *
	 * @param schemaStoreInfo schemaInfo for schema
	 * @return list of logical relations
	 */
	public static ArrayList<LogicalRelation> createLogicalRelations(SchemaInfo schemaStoreInfo) {
		// perform the relational chase
		ArrayList<LogicalRelation> logicalRelations = chase(schemaStoreInfo);

		// replace schemaInfo in each logical relation with
		for (LogicalRelation logRel : logicalRelations) {
			logRel.generateLogicalRelationSchemaInfo(schemaStoreInfo);
		}

		return logicalRelations;
	}


	/**
	 * chase: Perform the relational chase by following foreign key relationships
	 * @param inputSchemaInfo input schemaInfo to perform relational chase over
	 * @return list of LogicalRelations (one per entity in input schemaInfo
	 */
	private static ArrayList<LogicalRelation> chase(SchemaInfo inputSchemaInfo) {
		ArrayList<LogicalRelation> logicalRelations = new ArrayList<LogicalRelation>();
		// get entity set from SchemaStore schemaInfo
		ArrayList<Entity> entitySet = new ArrayList<Entity>();
		for (SchemaElement se : inputSchemaInfo.getElements(Entity.class)) {
			entitySet.add((Entity)se);
		}
		
		// create logical relation for each entity
		for (Entity entity : entitySet) {
			LogicalRelation currentLogicalRelation = new LogicalRelation();
			HashSet<Integer> seenEdges = new HashSet<Integer>();
			ArrayList<Entity> queue = new ArrayList<Entity>();
			queue.add(entity);
			currentLogicalRelation.getEntitySet().add(entity);
			Integer leftPathIndex =  null;
			Integer rightPathIndex = currentLogicalRelation.getEntitySet().lastIndexOf(entity);
	
			while (queue.size() > 0) {
				Entity leftPath = queue.remove(0);

				ArrayList<Relationship> relEdges = inputSchemaInfo.getRelationships(leftPath.getId());
				for (Relationship relationship : relEdges) {
					if (relationship.getLeftID().equals(leftPath.getId())&& !seenEdges.contains(relationship.getId())) {
						// update attribute sets; pathIndex is index of path in LogicalRelation's pathSet
						leftPathIndex = currentLogicalRelation.getEntitySet().lastIndexOf(leftPath);
						rightPathIndex = currentLogicalRelation.getEntitySet().size();
						ArrayList<Integer> indices = new ArrayList<Integer>();
						indices.add(leftPathIndex); indices.add(rightPathIndex);
						currentLogicalRelation.entityIndicesByRel.put(relationship.getId(),indices);
						
						currentLogicalRelation.getEntitySet().add((Entity)inputSchemaInfo.getElement(relationship.getRightID()));
						queue.add((Entity)inputSchemaInfo.getElement(relationship.getRightID()));
						seenEdges.add(relationship.getId());
					}
				}
			}
			logicalRelations.add(currentLogicalRelation);
		}
		return logicalRelations;
	}

} // end class LogicalRelation
