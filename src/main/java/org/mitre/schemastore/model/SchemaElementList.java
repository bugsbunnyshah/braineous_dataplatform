// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.util.ArrayList;
import java.util.Arrays;

/** Class for storing a list of schema elements */
public class SchemaElementList
{
	// Holds the schema elements
	private Entity[] entities;
	private Attribute[] attributes;
	private Domain[] domains;
	private DomainValue[] domainValues;
	private Relationship[] relationships;
	private Containment[] containments;
	private Subtype[] subtypes;
	private Synonym[] synonyms;
	private Alias[] aliases;
	
	/** Provides a default constructor for this class */
	public SchemaElementList() {}
	
	/** Constructs the list of schema elements */
	public SchemaElementList(SchemaElement[] schemaElements)
	{
		// Create arrays for each type of schema object
		ArrayList<Entity> entities = new ArrayList<Entity>();
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		ArrayList<Relationship> relationships = new ArrayList<Relationship>();
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<DomainValue> domainValues = new ArrayList<DomainValue>();
		ArrayList<Containment> containments = new ArrayList<Containment>();
		ArrayList<Subtype> subtypes = new ArrayList<Subtype>();
		ArrayList<Synonym> synonyms = new ArrayList<Synonym>();
		ArrayList<Alias> aliases = new ArrayList<Alias>();
		
		// Place the schema elements in the proper arrays
		for(SchemaElement schemaElement : schemaElements)
		{
			if(schemaElement instanceof Entity) entities.add((Entity)schemaElement);
			if(schemaElement instanceof Attribute) attributes.add((Attribute)schemaElement);
			if(schemaElement instanceof Domain) domains.add((Domain)schemaElement);
			if(schemaElement instanceof DomainValue) domainValues.add((DomainValue)schemaElement);
			if(schemaElement instanceof Relationship) relationships.add((Relationship)schemaElement);
			if(schemaElement instanceof Containment) containments.add((Containment)schemaElement);
			if(schemaElement instanceof Subtype) subtypes.add((Subtype)schemaElement);
			if(schemaElement instanceof Synonym) synonyms.add((Synonym)schemaElement);
			if(schemaElement instanceof Alias) aliases.add((Alias)schemaElement);
		}
		
		// Store the schema objects lists
		this.entities = entities.toArray(new Entity[0]);
		this.attributes = attributes.toArray(new Attribute[0]);
		this.domains = domains.toArray(new Domain[0]);
		this.domainValues = domainValues.toArray(new DomainValue[0]);
		this.relationships = relationships.toArray(new Relationship[0]);
		this.containments = containments.toArray(new Containment[0]);
		this.subtypes = subtypes.toArray(new Subtype[0]);
		this.synonyms = synonyms.toArray(new Synonym[0]);
		this.aliases = aliases.toArray(new Alias[0]);
	}
	
	// Handles all getters for this class
	public Entity[] getEntities() { return entities; }
	public Attribute[] getAttributes() { return attributes; }
	public Domain[] getDomains() { return domains; }
	public DomainValue[] getDomainValues() { return domainValues; }
	public Relationship[] getRelationships() { return relationships; }
	public Containment[] getContainments() { return containments; }
	public Subtype[] getSubtypes() { return subtypes; }
	public Synonym[] getSynonyms() { return synonyms; }
	public Alias[] getAliases() { return aliases; }

	// Handles all setters for this class
	public void setEntities(Entity[] entities) { this.entities = entities; }
	public void setAttributes(Attribute[] attributes) { this.attributes = attributes; }
	public void setDomains(Domain[] domains) { this.domains = domains; }
	public void setDomainValues(DomainValue[] domainValues) { this.domainValues = domainValues; }
	public void setRelationships(Relationship[] relationships) { this.relationships = relationships; }
	public void setContainments(Containment[] containments) { this.containments = containments; }
	public void setSubtypes(Subtype[] subtypes) { this.subtypes = subtypes; }
	public void setSynonyms(Synonym[] synonyms) { this.synonyms = synonyms; }
	public void setAliases(Alias[] aliases) { this.aliases = aliases; }

	/** Returns the schema elements */
	public SchemaElement[] geetSchemaElements()
	{
		ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
		schemaElements.addAll(Arrays.asList(entities));
		schemaElements.addAll(Arrays.asList(attributes));
		schemaElements.addAll(Arrays.asList(domains));
		schemaElements.addAll(Arrays.asList(domainValues));
		schemaElements.addAll(Arrays.asList(relationships));
		schemaElements.addAll(Arrays.asList(containments));
		schemaElements.addAll(Arrays.asList(subtypes));
		schemaElements.addAll(Arrays.asList(synonyms));
		schemaElements.addAll(Arrays.asList(aliases));
		return schemaElements.toArray(new SchemaElement[0]);
	}
}