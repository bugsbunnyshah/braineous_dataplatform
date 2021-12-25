// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.model;

import org.mitre.harmony.model.SchemaManager;
import org.mitre.rmap.generator.Dependency;
import org.mitre.schemastore.model.*;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

import java.util.ArrayList;
import java.util.HashMap;


public class RMapSchemaManager extends SchemaManager
{
	/** Stores the Harmony model */
	private RMapHarmonyModel harmonyModel;

	/** Caches schema information for Logical Relation "schemas" by Dependency */
	private HashMap<Integer,ProjectSchema> sourceSchemaIDForMappingID = null;
	private HashMap<Integer,ProjectSchema> targetSchemaIDForMappingID = null;
	private ArrayList<Mapping> mappingList = null;
	private HashMap<Integer, ArrayList<MappingCell>> mappingCellsByMapping = null;
	private HashMap<Dependency, Integer> mappingIDbyDependency = null;
	
	public RMapSchemaManager(RMapHarmonyModel harmonyModel) {
		super(harmonyModel);
		this.harmonyModel = harmonyModel;
	}

    public Boolean setMappingCells(Dependency dependency, ArrayList<MappingCell> newMappingCells){
        Integer mappingID = mappingIDbyDependency.get(dependency);
        if (mappingID != null) {
            mappingCellsByMapping.put(mappingID, newMappingCells);
            return true;
        } else {        
            return false;
        }
    }

    public Mapping getMapping(Integer mappingID){
        Mapping retVal = null;
        for (Mapping mapping : mappingList) {
            if (mapping.getId().equals(mappingID)) { retVal = mapping; }
        }
        return retVal;
    }

    /** Override -- Initializes the schema list */
    @SuppressWarnings("unchecked")
    public void initSchemas(ArrayList<Dependency> dependencyList) {
        schemas = new HashMap<Integer,Schema>();
        mappingList = new ArrayList<Mapping>();
        mappingCellsByMapping = new HashMap<Integer, ArrayList<MappingCell>>();

        sourceSchemaIDForMappingID = new HashMap<Integer,ProjectSchema>();
        targetSchemaIDForMappingID = new HashMap<Integer,ProjectSchema>();
        mappingIDbyDependency = new HashMap<Dependency, Integer>();

        try {
            for (Dependency dependency : dependencyList) {
                // generate the mapping, mapping matrix, and logical relation "schema" graphs
                Object[] mapInfo = dependency.generateMapping(harmonyModel.getProjectManager().getProject());

                Mapping mapping = (Mapping)mapInfo[0];
                ArrayList<MappingCell> mappingCells = (ArrayList<MappingCell>)mapInfo[1];
                HierarchicalSchemaInfo sourceGraph = (HierarchicalSchemaInfo)mapInfo[2];
                HierarchicalSchemaInfo targetGraph = (HierarchicalSchemaInfo)mapInfo[3];

                mappingList.add(mapping);
                mappingCellsByMapping.put(mapping.getId(), mappingCells);

                Schema sourceSchema = sourceGraph.getSchema();
                Schema targetSchema = targetGraph.getSchema();

                // set the source and target ProjectSchema
                sourceSchemaIDForMappingID.put(mapping.getId(), new ProjectSchema(sourceSchema.getId(), sourceSchema.getName(), "RMap"));
                targetSchemaIDForMappingID.put(mapping.getId(), new ProjectSchema(targetSchema.getId(), targetSchema.getName(), "RMap"));
                mappingIDbyDependency.put(dependency, mapping.getId());

	            // add schemas for the logical relations
	            schemas.put(sourceGraph.getSchema().getId(), sourceGraph.getSchema());
	            schemas.put(targetGraph.getSchema().getId(), targetGraph.getSchema());

                // add a graph for each logical relation
                schemaInfoList.put(sourceGraph.getSchema().getId(), sourceGraph);
                schemaInfoList.put(targetGraph.getSchema().getId(), targetGraph);

                // add all the schemaElements
                for (SchemaElement se : sourceGraph.getElements(null)) {
                    schemaElements.put(se.getId(), se);
                }

                for (SchemaElement se : targetGraph.getElements(null)) {
                    schemaElements.put(se.getId(), se);
                }
            }
        } catch(Exception e) {
            System.err.println("[E] RMapSchemaManager.initSchemas - " + e.getMessage());
            e.printStackTrace();
        }
    }

    // overrides the SchemaManager initSchemas method
    // this is because the parent will get ALL schemas and our subclass only wants
    // to get certain schemas as defined in the new initSchemas method shown above
    public void initSchemas() {
    	return;
    }

    public ArrayList<MappingCell> getMappingCells(Integer mappingID) {
    	if (mappingCellsByMapping == null) { return null; }
        return mappingCellsByMapping.get(mappingID);
    }
    
    public Integer getMappingID(Dependency dependency) {
    	if (mappingIDbyDependency == null) { return null; }
    	return mappingIDbyDependency.get(dependency);
    }
    
    public ProjectSchema getSourceSchema(Integer mappingID) {
    	if (sourceSchemaIDForMappingID == null) { return null; }
    	return sourceSchemaIDForMappingID.get(mappingID);
    }
    
    public ProjectSchema getTargetSchema(Integer mappingID) {
    	if (targetSchemaIDForMappingID == null) { return null; }
    	return targetSchemaIDForMappingID.get(mappingID);
    }

	/** @Override -- Returns the deletable schemas */
	public ArrayList<Integer> getDeletableSchemas() {
		return new ArrayList<Integer>();
	}

	//--------------------------
	// Schema Element Functions
	//--------------------------
	
	/** @Override - Returns the graph for the specified schema */
	public HierarchicalSchemaInfo getSchemaInfo(Integer schemaID) {
		if (!schemaInfoList.containsKey(schemaID)) {
			System.err.println("[E] RMapSchemaManager.getSchemaInfoList -- trying to use undefined schemaInfo " + schemaID);
		}
		return schemaInfoList.get(schemaID);
	}
	
	/** @Override - Gets the specified schema element */
	public SchemaElement getSchemaElement(Integer schemaElementID) {
		if (!schemaElements.containsKey(schemaElementID)) {
			System.err.println("[E] RMapSchemaManager.getSchemaElement -- trying to use undefined schemaElement " + schemaElementID);
		}
		return schemaElements.get(schemaElementID);
	}
}
