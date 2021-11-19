package org.mitre.schemastore.porters.xml;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.FunctionImp;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.MappingCellInput;
import org.mitre.schemastore.model.ProjectSchema;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.Synonym;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.porters.mappingImporters.MappingCellPaths;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Class for converting SchemaStore objects from XML */
public class ConvertFromXML
{
	/** Retrieves child elements from the element */
	static private ArrayList<Element> getElements(Element element, String tag)
	{
		ArrayList<Element> elements = new ArrayList<Element>();
		NodeList tagList = element.getElementsByTagName(tag);
		if(tagList!=null)
			for(int i=0; i<tagList.getLength(); i++)
				elements.add((Element)tagList.item(i));
		return elements;
	}
	
	/** Retrieves values from the element */
	static private ArrayList<String> getValues(Element element, String tag)
	{
		ArrayList<String> values = new ArrayList<String>();
		for(Element childElement : getElements(element,tag))
		{
			if (childElement.getFirstChild() != null) {
				String value = childElement.getFirstChild().getNodeValue();
				if (!value.equals("NULL"))
				{
					values.add(value);	
				}
			}
		}

		return values;
	}
	
	/** Retrieves integer values from the element */
	static private ArrayList<Integer> getIntegerValues(Element element, String tag)
	{
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(String value : getValues(element,tag))
			try { values.add(Integer.parseInt(value)); } catch(Exception e) {}
		return values;
	}

	
	/** Retrieves a value from the element */
	static private String getValue(Element element, String tag)
		{ 
			try {
				ArrayList<String> values = getValues(element, tag);
				if (!values.isEmpty()){
					return getValues(element,tag).get(0);}
				else {
					return null;
				} 
			}catch(Exception e) { 
				return null; 
			} 
		}
	
	/** Retrieves an integer value from the element */
	static private Integer getIntegerValue(Element element, String tag)
		{ try { return Integer.parseInt(getValue(element,tag)); } catch(Exception e) { return null; } }
	
	/** Retrieves a double value from the element */
	static private Double getDoubleValue(Element element, String tag)
		{ try { return Double.parseDouble(getValue(element,tag)); } catch(Exception e) { return null; }}

	/** Retrieves a boolean value from the element */
	static private Boolean getBooleanValue(Element element, String tag)
		{ try { return new Boolean(getValue(element,tag)); } catch(Exception e) { return null; }}

	/** Retrieves a date value from the element */
	static private Date getDateValue(Element element, String tag)
	{
		try { return DateFormat.getDateInstance(DateFormat.MEDIUM).parse(getValue(element,tag)); }
		catch(Exception e) { return null; }
	}
	
	/** Retrieve the schema from the specified XML */
	static public Schema getSchema(Element element)
	{
		Schema schema = new Schema();
		schema.setId(getIntegerValue(element,"SchemaId"));
		schema.setName(getValue(element,"SchemaName"));
		schema.setAuthor(getValue(element,"SchemaAuthor"));
		schema.setSource(getValue(element,"SchemaSource"));
		schema.setType(getValue(element,"SchemaType"));
		schema.setDescription(getValue(element,"SchemaDescription"));
		schema.setLocked(getBooleanValue(element,"SchemaLocked"));
		return schema;
	}
	
	/** Retrieve the parent schemas from the specified XML */
	static public ArrayList<Integer> getParentSchemaIDs(Element element)
		{ return getIntegerValues(element,"SchemaParentId"); }
	
	/** Retrieves the schema element from the specified XML */
	static public SchemaElement getSchemaElement(Element element)
	{
		// Identify the schema element to be constructed
		SchemaElement schemaElement = null;
		String type = element.getTagName();
		if(type.equals(Alias.class.getName())) schemaElement = new Alias();
		if(type.equals(Attribute.class.getName())) schemaElement = new Attribute();
		if(type.equals(Containment.class.getName())) schemaElement = new Containment();
		if(type.equals(Domain.class.getName())) schemaElement = new Domain();
		if(type.equals(DomainValue.class.getName())) schemaElement = new DomainValue();
		if(type.equals(Entity.class.getName())) schemaElement = new Entity();
		if(type.equals(Relationship.class.getName())) schemaElement = new Relationship();
		if(type.equals(Subtype.class.getName())) schemaElement = new Subtype();
		if(schemaElement==null) return null;
		
		// Populate the general schema element information
		schemaElement.setId(getIntegerValue(element,"ElementId"));
		schemaElement.setName(getValue(element,"ElementName"));
		schemaElement.setDescription(getValue(element,"ElementDescription"));
		schemaElement.setBase(getIntegerValue(element,"ElementBase"));		
		
		// Populate alias schema elements
		if(schemaElement instanceof Alias)
			((Alias)schemaElement).setElementID(getIntegerValue(element,"AliasIdElement"));

		// Populate attribute schema elements
		if(schemaElement instanceof Attribute)
		{
			Attribute attribute = (Attribute)schemaElement;
			attribute.setEntityID(getIntegerValue(element,"AttributeEntityId"));
			attribute.setDomainID(getIntegerValue(element,"AttributeDomainId"));
			attribute.setMin(getIntegerValue(element,"AttributeMin"));
			attribute.setMax(getIntegerValue(element,"AttributeMax"));
			attribute.setKey(getBooleanValue(element,"AttributeKey"));
		}
		
		// Populate containment schema elements
		if(schemaElement instanceof Containment)
		{
			Containment containment = (Containment)schemaElement;
			containment.setParentID(getIntegerValue(element,"ContainmentParentId"));
			containment.setChildID(getIntegerValue(element,"ContainmentChildId"));
			containment.setMin(getIntegerValue(element,"ContainmentMin"));
			containment.setMax(getIntegerValue(element,"ContainmentMax"));			
		}
		
		// Populate domain value schema elements
		if(schemaElement instanceof DomainValue)
			((DomainValue)schemaElement).setDomainID(getIntegerValue(element,"DomainValueDomainId"));

		// Populate relationship schema elements
		if(schemaElement instanceof Relationship)
		{
			Relationship relationship = (Relationship)schemaElement;
			relationship.setLeftID(getIntegerValue(element,"RelationshipLeftId"));
			relationship.setLeftMin(getIntegerValue(element,"RelationshipLeftMin"));
			relationship.setLeftMax(getIntegerValue(element,"RelationshipLeftMax"));
			relationship.setRightID(getIntegerValue(element,"RelationshipRightId"));
			relationship.setRightMin(getIntegerValue(element,"RelationshipRightMin"));
			relationship.setRightMax(getIntegerValue(element,"RelationshipRightMax"));
		}
		
		// Populate subtype schema elements
		if(schemaElement instanceof Subtype)
		{
			Subtype subtype = (Subtype)schemaElement;
			subtype.setParentID(getIntegerValue(element,"SubTypeParentId"));
			subtype.setChildID(getIntegerValue(element,"SubTypeChildId"));
		}

		// Populate synonym schema elements
		if(schemaElement instanceof Synonym)
			((Synonym)schemaElement).setElementID(getIntegerValue(element,"SynonymIdElement"));
		
		return schemaElement;
	}

	/** Retrieve the function from the specified XML */
	static public Function getFunction(Element element)
	{
		// Populate the function
		Function function = new Function();
		function.setId(getIntegerValue(element,"FunctionId"));
		function.setName(getValue(element,"FunctionName"));
		function.setDescription(getValue(element,"FunctionDescription"));
		function.setExpression(getValue(element,"FunctionExpression"));
		function.setCategory(getValue(element,"FunctionCategory"));
		function.setInputTypes(getIntegerValues(element,"FunctionInputType").toArray(new Integer[0]));
		function.setOutputType(getIntegerValue(element,"FunctionOutputType"));
		return function;
	}
	
	/** Retrieve the function implementations from the specified XML */
	static public ArrayList<FunctionImp> getFunctionImps(Element element)
	{
		// Retrieve the function ID
		Integer functionID = getIntegerValue(element,"FunctionId");
		
		// Populate the function implementations
		ArrayList<FunctionImp> functionImps = new ArrayList<FunctionImp>();
		for(Element functionImpElement : getElements(element,"FunctionImp"))
		{
			FunctionImp functionImp = new FunctionImp();
			functionImp.setFunctionID(functionID);
			functionImp.setLanguage(getValue(functionImpElement,"FunctionImpLanguage"));
			functionImp.setDialect(getValue(functionImpElement,"FunctionImpDialect"));
			functionImp.setImplementation(getValue(functionImpElement,"FunctionImpImplementation"));
			functionImps.add(functionImp);
		}
		return functionImps;
	}
	
	/** Retrieve information on the source schema from the specified XML */
	static public ProjectSchema getSourceSchema(Element element)
	{
		ProjectSchema schema = new ProjectSchema();
		schema.setId(getIntegerValue(element,"MappingSourceId"));
		schema.setName(getValue(element,"MappingSourceName"));
		schema.setModel(getValue(element,"MappingSourceModel"));		
		return schema;
	}

	/** Retrieve information on the target schema from the specified XML */
	static public ProjectSchema getTargetSchema(Element element)
	{
		ProjectSchema schema = new ProjectSchema();
		schema.setId(getIntegerValue(element,"MappingTargetId"));
		schema.setName(getValue(element,"MappingTargetName"));
		schema.setModel(getValue(element,"MappingTargetModel"));		
		return schema;
	}
	
	/** Retrieve the mapping from the specified XML */
	static public Mapping getMapping(Element element)
	{
		Mapping mapping = new Mapping();
		mapping.setId(getIntegerValue(element,"MappingId"));
		mapping.setProjectId(getIntegerValue(element,"MappingProjectId"));
		mapping.setSourceId(getIntegerValue(element,"MappingSourceId"));
		mapping.setTargetId(getIntegerValue(element,"MappingTargetId"));
		return mapping;
	}
	
	/** Retrieve the elementID for the given element path */
	static private Integer getElementId(String pathString, HierarchicalSchemaInfo schemaInfo)
	{
		// Extract the path ID (used to help with ambiguous paths)
		pathString = pathString.replaceFirst("/","");
		Integer pathID = Integer.valueOf(pathString.replaceAll(".*:",""));
		pathString = pathString.replaceFirst(":[^:]+$","");
		
		// Retrieve the schema and path
		ArrayList<String> path = new ArrayList<String>();
		for(String element : new ArrayList<String>(Arrays.asList(pathString.split("/"))))
			path.add(element.replaceAll("&#47;","/"));
		
		// Retrieve the element ID
		ArrayList<Integer> elementIDs = schemaInfo.getPathIDs(path);
		if(elementIDs.size()>pathID) return elementIDs.get(pathID);
		if(elementIDs.size()>0) return elementIDs.get(0);
		return null;
	}
	
	/** Retrieve the mapping cell paths from the specified XML */
	static public MappingCellPaths getMappingCellPaths(Element element) throws Exception
	{	
		// Retrieve the mapping cell input paths
		ArrayList<String> inputPaths = new ArrayList<String>();
		for(Element inputElement : getElements(element,"MappingCellInput"))
			inputPaths.add(getValue(inputElement,"MappingCellInputPath"));

		// Retrieve the mapping cell output paths
		String outputPath = getValue(element,"MappingCellOutputPath");
	
		// Return the generated mapping cell
		return new MappingCellPaths(inputPaths, outputPath);
	}	
	
	/** Retrieve the mapping cell from the specified XML */
	static public MappingCell getMappingCell(Element element, HierarchicalSchemaInfo sourceInfo, HierarchicalSchemaInfo targetInfo) throws Exception
	{
		// Retrieve the mapping cell elements
		Integer id = getIntegerValue(element,"MappingCellId");
		String author = getValue(element,"MappingCellAuthor");
		Date date = getDateValue(element,"MappingCellDate");
		Double score = getDoubleValue(element,"MappingCellScore");
		Integer functionID = getIntegerValue(element,"MappingCellFunctionId");
		String notes = getValue(element,"MappingCellNotes");
		Integer outputID = getElementId(getValue(element,"MappingCellOutputPath"),targetInfo);
		if(outputID==null) return null;
	
		// Retrieve the mapping cell inputs
		ArrayList<MappingCellInput> inputs = new ArrayList<MappingCellInput>();
		for(Element inputElement : getElements(element,"MappingCellInput"))
		{
			MappingCellInput input = MappingCellInput.parse(getValue(inputElement,"MappingCellInputId"));
			if(input!=null && input.isConstant()) inputs.add(input);
			else
			{
				Integer inputID = getElementId(getValue(inputElement,"MappingCellInputPath"),sourceInfo);
				if(inputID==null) return null;
				inputs.add(new MappingCellInput(inputID));
			}
		}
		
		// Return the generated mapping cell
		return new MappingCell(id, null, inputs.toArray(new MappingCellInput[0]), outputID, score, functionID, author, date, notes);
	}
}