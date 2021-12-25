// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.generator;

import org.mitre.schemastore.client.SchemaStoreClient;
import org.mitre.schemastore.model.*;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;

import java.util.ArrayList;

public class Dependency {

	// store source logical relation
	private LogicalRelation sourceLogicalRelation;

	// store target logical relation
	private LogicalRelation targetLogicalRelation;

	// store set of constraints between source and target from mapping function
	private ArrayList<MappingCell> coveredCorrespondences;

	// constant ID assigned to the base type 
	private static final Integer INTEGER_DOMAIN_ID = -1;
	public void updateCorrespondences(ArrayList<MappingCell> newCellList){
		coveredCorrespondences = new ArrayList<MappingCell>();
		for (MappingCell corr : newCellList) {
			this.coveredCorrespondences.add(corr);
		}
	}

	public Dependency(LogicalRelation source, LogicalRelation target, ArrayList<MappingCell> corrs) {
		sourceLogicalRelation = source.copy();
		targetLogicalRelation = target.copy();
		coveredCorrespondences = corrs;
	}

	public ArrayList<MappingCell> getCoveredCorrespondences() {
		return coveredCorrespondences;
	}

	public LogicalRelation getSourceLogicalRelation() {
		return sourceLogicalRelation;
	}

	public LogicalRelation getTargetLogicalRelation() {
		return targetLogicalRelation;
	}

	// generate all of the information necessary to represent the dependency
	// as a mapping
	public Object[] generateMapping(Project project) {
		Object[] value = new Object[4];
		
		// generate return Mapping
		Schema sourceSchema = sourceLogicalRelation.getMappingSchemaInfo().getSchema();
		Schema targetSchema = targetLogicalRelation.getMappingSchemaInfo().getSchema();
		Mapping mapping = new Mapping(LogicalRelation.getNextID(), project.getId(), sourceSchema.getId(), targetSchema.getId());

		ArrayList<MappingCell> mappingCells = new ArrayList<MappingCell>();
		for (MappingCell corr : coveredCorrespondences) {
			// create copy of mapping cell for correspondence
			MappingCell cell = corr.copy();
			cell.setId(LogicalRelation.getNextID());
			cell.setMappingId(mapping.getId());

			try {
				Integer sourceCount = sourceLogicalRelation.getIDmappings_SS_to_LR().get(sourceLogicalRelation.getIDmappings_LR_to_SS().get(cell.getElementInputIDs()[0])).size();
				Integer targetCount = targetLogicalRelation.getIDmappings_SS_to_LR().get(targetLogicalRelation.getIDmappings_LR_to_SS().get(cell.getOutput())).size();
				// TODO: Need to modify here to add "color" back to lines
				if (sourceCount > 1 || targetCount > 1) {
					cell.setScore(0.1);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			// actually put the mapping cell into our list
			mappingCells.add(cell);
		}

		this.coveredCorrespondences = mappingCells;
		value[0] = mapping;
		value[1] = mappingCells;
		value[2] = sourceLogicalRelation.getMappingSchemaInfo();
		value[3] = targetLogicalRelation.getMappingSchemaInfo();
		return value;
	} // end method

	// generateDependencies(): For each pair of source and target logical relations,
	// 	generate a dependency for each possible coverage of covered correspondences 
	static public ArrayList<Dependency> generateDependencies(ArrayList<LogicalRelation> sourceLogRels, ArrayList<LogicalRelation> targetLogRels, ArrayList<MappingCell> correspondences) {
		ArrayList<Dependency> retVal = new ArrayList<Dependency>();
		for (LogicalRelation sourceLogRel : sourceLogRels) {
			for (LogicalRelation targetLogRel : targetLogRels) {
				ArrayList<MappingCell> coveredCorrespondences = new ArrayList<MappingCell>();
				for (MappingCell correspondence : correspondences)
				{
					MappingCellInput[] inputs = sourceLogRel.translate_SS_to_LR(correspondence.getInputs());
					ArrayList<Integer> outputs = targetLogRel.getIDmappings_SS_to_LR().get(correspondence.getOutput());
					if(inputs!=null && outputs!=null)
					{						
						MappingCell correspondenceCopy = correspondence.copy();
						correspondenceCopy.setInputs(inputs);
						correspondenceCopy.setOutput(outputs.get(0));
						coveredCorrespondences.add(correspondenceCopy);
					}
				}
			
				if (coveredCorrespondences.size() > 0) {
					retVal.add(new Dependency(sourceLogRel, targetLogRel, coveredCorrespondences));
				}
			}
		}

		return retVal;
	}

	public static ArrayList<Dependency> generate(SchemaStoreClient client, Integer mappingID) {
		// create logical relations for source / target schemas
		ArrayList<Dependency> retVal = new ArrayList<Dependency>();
		ArrayList<LogicalRelation> allSourceLogRels = new ArrayList<LogicalRelation>();
		ArrayList<LogicalRelation> allTargetLogRels = new ArrayList<LogicalRelation>();
		
		// find source / target graphs mentioned in the selected mapping
		try {
			// don't have to run a for loop anymore because
			// 1. we don't have the ProjectSchema objects contained in the Mapping object
			// 2. we only have a source and a target and the ID for both is in the Mapping object
			// 3. the source/target id lines up with both the Schema and ProjectSchema
			
			SchemaInfo sourceGraph = client.getSchemaInfo(client.getMapping(mappingID).getSourceId()).copy();
			renderRelational(sourceGraph);
			allSourceLogRels.addAll(LogicalRelation.createLogicalRelations(sourceGraph));

			SchemaInfo targetGraph = client.getSchemaInfo(client.getMapping(mappingID).getTargetId()).copy();
			renderRelational(targetGraph);
			allTargetLogRels.addAll(LogicalRelation.createLogicalRelations(targetGraph));

			// get correspondences
			ArrayList<MappingCell> schemaStoreCorrespondences = client.getMappingCells(mappingID);

			// create dependencies -- for each dependency, generate mapping graphs for each logical relation
			retVal = Dependency.generateDependencies(allSourceLogRels, allTargetLogRels, schemaStoreCorrespondences);
		} catch (Exception e){
			e.printStackTrace();
		}
		return retVal;
	} // end method generate()

	
	/**
	 * renderRelational: 
	 * @param input
	 * @return
	 */
	private static void renderRelational(SchemaInfo inGraph){
		Integer maxID = 0;
		for (SchemaElement se : inGraph.getElements(null)) {
			if (maxID < se.getId()) { maxID = se.getId(); }
		}
		maxID++;
		
		for (SchemaElement se : inGraph.getElements(Relationship.class)) {
			Relationship rel = (Relationship) se;
			
			// make sure LEFT FK --> RIGHT
			if (rel.getRightMax() == null || rel.getRightMax() != 1) {
				Integer temp = rel.getRightID();
				rel.setRightID(rel.getLeftID());
				rel.setLeftID(temp);
				
				temp = rel.getRightMin();
				rel.setRightMin(rel.getLeftMin());
				rel.setLeftMin(temp);
				
				temp = rel.getRightMax();
				rel.setRightMax(rel.getLeftMax());
				rel.setLeftMax(temp);
			} 
		} 
		
		// if graph contains containments -- add ID column to entity
		if (inGraph.getElements(Containment.class).size() > 0) {
			if (inGraph.getElement(Dependency.INTEGER_DOMAIN_ID) == null) {
				if (!inGraph.addElement(new Domain(-1,"Integer","The Integer domain",inGraph.getSchema().getId()))) {
					System.err.println("[E] Dependency:renderRelational -- failed to add Integer domain");
				}
			}
			for (SchemaElement se : inGraph.getElements(Entity.class)) {
				Entity entity = (Entity)se;
				Attribute entityKey = new Attribute(maxID++,"ID","",entity.getId(), Dependency.INTEGER_DOMAIN_ID,1,1,true,inGraph.getSchema().getId());
				if (!inGraph.addElement(entityKey)) {
					System.err.println("[E] Dependency:renderRelational -- failed to add entityKey for entity " + entity.getName());
				}
			}
		}
		
		// replace each containment
		for (SchemaElement se : inGraph.getElements(Containment.class)) {
			Containment cont = (Containment) se;
			SchemaElement child = inGraph.getElement(cont.getChildID());
			
			// CASE: schema --> (entity | domain) ==> delete
			if (cont.getParentID() == null) {
				if (!inGraph.deleteElement(cont.getId())) {
					System.err.println("[E] Dependency:renderRelational -- failed to delete schema-level containement");
				}
			}
			// CASE: entity --> entity ==> replace with relationship 
			else if (child instanceof Entity){
				Relationship rel = new Relationship(cont.getId(),cont.getName(),cont.getDescription(),cont.getChildID(),cont.getMin(),cont.getMax(),cont.getParentID(),1,1,inGraph.getSchema().getId());
				if (!inGraph.deleteElement(cont.getId()))
					System.err.println("[E] Dependency:renderRelational -- failed to delete containement " + cont.getName());
				if (inGraph.addElement(rel) == false)
					System.err.println("[E] Dependency:renderRelational -- failed to add relationship " + rel.getName());
			}
			
			// CASE: entity --> domain ==>
			//		a) max > 1 --> replace with table
			//		b) max == 1 --> replace with attribute
			
			else if (child instanceof Domain) {
				if (cont.getMax() != 1) {
					String name = cont.getName() +"." + inGraph.getElement(cont.getParentID()).getName();
					Entity domEntity = new Entity(maxID++,name,"desc",inGraph.getSchema().getId());
					Relationship rel = new Relationship(maxID++,name,cont.getDescription(),domEntity.getId(),cont.getMin(),cont.getMax(),cont.getParentID(),1,1,inGraph.getSchema().getId());
					Attribute domAttr = new Attribute(cont.getId(),cont.getName(),cont.getDescription(),domEntity.getId(),child.getId(),0,1,false,inGraph.getSchema().getId());
					Attribute domEntityKey = new Attribute(maxID++,"ID","",domEntity.getId(), Dependency.INTEGER_DOMAIN_ID,1,1,true,inGraph.getSchema().getId());

					if (!inGraph.deleteElement(cont.getId())) {
						System.err.println("[E] Dependency:renderRelational -- failed to delete containement");
					}
					if (!inGraph.addElement(domEntity)) {
						System.err.println("[E] Dependency:renderRelational -- failed to add domEntity");
					}
					if (!inGraph.addElement(rel)) {
						System.err.println("[E] Dependency:renderRelational -- failed to add relationship");
					}
					if (!inGraph.addElement(domAttr)) {
						System.err.println("[E] Dependency:renderRelational -- failed to add domAttr");
					}
					if (!inGraph.addElement(domEntityKey)) {
						System.err.println("[E] Dependency:renderRelational -- failed to add domEntityKey");
					}
				} else {
					Attribute attr = new Attribute(cont.getId(),cont.getName(),cont.getDescription(),cont.getParentID(),cont.getChildID(),cont.getMin(),cont.getMax(),false,inGraph.getSchema().getId());

					if (!inGraph.deleteElement(cont.getId())) {
						System.err.println("[E] Dependency:renderRelational -- failed to delete containement");
					}
					if (!inGraph.addElement(attr)) {
						System.err.println("[E] Dependency:renderRelational -- failed to add attribute " + attr.getName());
					}
				}
			}
		}
	}
		
} // end class
