// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.
package org.mitre.rmap.generator;

import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;

import java.util.ArrayList;
import java.util.HashMap;

/** stores (pathID, attributeID) pairs */
class IdPair {
	
	Integer _pathId;
	Integer _attributeId;
	
	public IdPair(Integer f, Integer s){
		_pathId = f;
		_attributeId = s;
	}
		
	public IdPair(IdPair info){
		_pathId = info.getPathId();
		_attributeId = info.getAttributeId();
	}
	
	public Integer getPathId(){ return _pathId; }
	public Integer getAttributeId() { return _attributeId; }



	public String toString(){ return new String(_pathId + "--" + _attributeId); }
		
	public boolean equals(IdPair idp){
		return idp._pathId.equals(_pathId) && idp._attributeId.equals(_attributeId);
	}
	
	public int hashCode() { 
	    int hash = 1;
	    hash = hash * 31 + _pathId.hashCode();
	    hash = hash * 31 
	                + (_attributeId == null ? 0 : _attributeId.hashCode());
	    return hash;
	}

} // end class IdPair

/** stores (relationshipID, leftPathIndex, rightPathIndex, rightAttrId) triples */

class IdTriple {
	private Integer relationshipId;
	private Integer leftPathIndex;
	private Integer rightPathIndex;
	private Integer rightAttributeId;
	
	public IdTriple(Integer relationshipId, Integer leftPathIndex, Integer rightPathIndex, Integer rightAttributeId){
		this.relationshipId = relationshipId;
		this.leftPathIndex = leftPathIndex;
		this.rightPathIndex = rightPathIndex;
		this.rightAttributeId = rightAttributeId;
	}

	public IdTriple copy(){
		return new IdTriple(relationshipId, leftPathIndex, rightPathIndex,  rightAttributeId);
	}
	
	private Integer getKey(Integer entityID, SchemaInfo g){
		ArrayList<Attribute> attrs = g.getAttributes(entityID);
		for (Attribute a : attrs) {
			if (a.isKey() == true) { return a.getId(); }
		}
		System.err.println("[E] NRI:getKey -- no attribute was assigned as key for entity");
		return null;	
	}
	
	/**
	 * Convention: IdTriples go from N to 1
	 * 
	 * In other words, we assume that relation between Entity A, B is 
	 * 1) 1-n  (B.attr references to A.attr, A.attr is key) OR 
	 * 2) n-1 (A.attr references to B.attr, B.attr is key) 
	 * 
	 * i.e., managerID  INTEGER REFERENCES EMP(empID)); 
	 * managerID is UNIVERSAL (field in emp relation, empID is EXISTENTIAL (empID is KEY in emp relation)
	 * 
	 * @param relationship Relationship 
	 * @param graph Schema Graph
	 */
	public IdTriple(Relationship relationship, SchemaInfo graph){
		
		relationshipId = relationship.getId();
		// if the LEFT entity is the key, swap left and right 
		if ((relationship.getLeftMax() != null) && (relationship.getLeftMax() == 1)){
			rightPathIndex = relationship.getLeftID();
			rightAttributeId = getKey(relationship.getLeftID(), graph);
			leftPathIndex = relationship.getRightID();
			
		} else { // if the RIGHT entity is the key, just copy 
			rightPathIndex = relationship.getRightID();
			rightAttributeId = getKey(relationship.getRightID(), graph); 
			leftPathIndex = relationship.getLeftID();
			
		} 
	} // end constructor
	
	static public HashMap<Integer, IdTriple> createTripleSet(SchemaInfo schemaGraph, ArrayList<Entity> entitySet){
		HashMap<Integer, IdTriple> value = new HashMap<Integer, IdTriple>();
		for (SchemaElement rel : schemaGraph.getElements(Relationship.class)) {
			value.put(rel.getId(),  new IdTriple((Relationship) rel, schemaGraph));
		}
		return value;
	}
	
	
	//exports IdTriple as clause: [TABLE_CHAR][PATH-INDEX].[rel.name] = [TABLE_CHAR][PATH-INDEX].[rightAttr.name]
	public String toClause(SchemaInfo schemaGraph){
		Relationship rel = (Relationship)schemaGraph.getElement(getRelationshipId());
		return new String(SQLGenerator.TABLE_CHAR + leftPathIndex + "." +  rel.getName() + " = " + SQLGenerator.TABLE_CHAR + rightPathIndex + "." + schemaGraph.getElement(rightAttributeId));
	}
	
	public Integer getLeftPathId() { return leftPathIndex; }
	public Integer getRightPathId() { return rightPathIndex; }
	public Integer getRelationshipId() { return relationshipId; }
	public Integer getRightAttrId() { return rightAttributeId;}
	

} // end class IDTriple
