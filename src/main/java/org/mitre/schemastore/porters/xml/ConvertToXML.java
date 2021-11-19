package org.mitre.schemastore.porters.xml;

import java.text.DateFormat;
import java.util.ArrayList;

import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.FunctionImp;
import org.mitre.schemastore.model.Mapping;
import org.mitre.schemastore.model.MappingCell;
import org.mitre.schemastore.model.MappingCellInput;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.Synonym;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Class for converting SchemaStore objects to XML */
public class ConvertToXML
{
	/** Stores a reference to the XML document */
	static private Document document;
	
	/** Adds a new child element to the specified element */
	static private Element addElement(Element element, String name, Object object)
	{
		Element childElement = document.createElement(name);
		String text = object==null ? "" : object.toString().replaceAll("[^\\p{ASCII}]","#");
		childElement.appendChild(document.createTextNode(text));
		element.appendChild(childElement);
		return childElement;
	}
	
	/** Generates the XML for the specified schema */
	static public Element generate(Schema schema, ArrayList<Integer> parentIDs, Document documentIn)
	{
		document = documentIn;
		
		// Generate the schema XML element
		Element element = document.createElement("Schema");			
		addElement(element,"SchemaId",schema.getId());
		addElement(element,"SchemaName", schema.getName());
		addElement(element,"SchemaAuthor", schema.getAuthor());
		addElement(element,"SchemaSource", schema.getSource());
		addElement(element,"SchemaType", schema.getType());
		addElement(element,"SchemaDescription", schema.getDescription());
		addElement(element,"SchemaLocked", new Boolean(schema.getLocked()));
		
		// Generate the schema parents
		if(parentIDs!=null)
			for(Integer parentID : parentIDs)
				addElement(element,"SchemaParentId",parentID);
		
		return element;
	}
	
	/** Generate XML for the specified schema element */
	static public Element generate(SchemaElement schemaElement, Document documentIn)
	{
		document = documentIn;

		// Generate XML for the generic schema element information
		Element element = document.createElement(schemaElement.getClass().getName());
		addElement(element,"ElementId",schemaElement.getId());
		addElement(element,"ElementName",schemaElement.getName());
		addElement(element,"ElementDescription",schemaElement.getDescription());
		addElement(element,"ElementBase",schemaElement.getBase());
		
		// Generate XML for alias schema elements
		if(schemaElement instanceof Alias)
			addElement(element,"AliasIdElement",((Alias)schemaElement).getElementID());

		// Generate XML for attribute schema elements
		if(schemaElement instanceof Attribute)
		{
			Attribute attribute = (Attribute)schemaElement;
			addElement(element,"AttributeEntityId", attribute.getEntityID());
			addElement(element,"AttributeDomainId", attribute.getDomainID());
			addElement(element,"AttributeMin", attribute.getMin());
			addElement(element,"AttributeMax", attribute.getMax());
			addElement(element,"AttributeKey", new Boolean(attribute.isKey()));
		}
		
		// Generate XML for containment schema elements
		if(schemaElement instanceof Containment)
		{
			Containment containment = (Containment)schemaElement;
			addElement(element,"ContainmentParentId",containment.getParentID()==null?"NULL":containment.getParentID());
			addElement(element,"ContainmentChildId",containment.getChildID());
			addElement(element,"ContainmentMin",containment.getMin());
			addElement(element,"ContainmentMax",containment.getMax());
		}
		
		// Generate XML for domain value schema elements
		if(schemaElement instanceof DomainValue)
			addElement(element,"DomainValueDomainId",((DomainValue)schemaElement).getDomainID());

		// Generate XML for relationship schema elements
		if(schemaElement instanceof Relationship)
		{
			Relationship relationship = (Relationship)schemaElement;
			addElement(element,"RelationshipLeftId",relationship.getLeftID());
			addElement(element,"RelationshipLeftMin",relationship.getLeftMin());
			addElement(element,"RelationshipLeftMax",relationship.getLeftMax());
			addElement(element,"RelationshipRightId",relationship.getRightID());
			addElement(element,"RelationshipRightMin",relationship.getRightMin());
			addElement(element,"RelationshipRightMax",relationship.getRightMax());
		}
		
		// Generate XML for subtype schema elements
		if(schemaElement instanceof Subtype)
		{
			Subtype subtype = (Subtype)schemaElement;
			addElement(element,"SubTypeParentId",subtype.getParentID());
			addElement(element,"SubTypeChildId",subtype.getChildID());
		}

		// Generate XML for synonym schema elements
		if(schemaElement instanceof Synonym)
			addElement(element,"SynonymIdElement",((Synonym)schemaElement).getElementID());
		
		return element;
	}	
	
	/** Generates the XML for the specified mapping */
	static public Element generate(Mapping mapping, HierarchicalSchemaInfo sourceInfo, HierarchicalSchemaInfo targetInfo, Document documentIn)
	{
		document = documentIn;

		// Generate the mapping XML elements
		Element element = document.createElement("Mapping");
		addElement(element,"MappingId",mapping.getId());
		addElement(element,"MappingProjectId",mapping.getProjectId());
		addElement(element,"MappingSourceId",mapping.getSourceId());
		addElement(element,"MappingSourceName",sourceInfo.getSchema().getName());
		addElement(element,"MappingSourceModel",sourceInfo.getModel().getClass().getName());
		addElement(element,"MappingTargetId",mapping.getTargetId());
		addElement(element,"MappingTargetName",targetInfo.getSchema().getName());
		addElement(element,"MappingTargetModel",targetInfo.getModel().getClass().getName());
		return element;
	}
	
	/** Generates the XML for the specified function */
	static public Element generate(Function function, ArrayList<FunctionImp> functionImps, Document documentIn)
	{
		document = documentIn;
		
		// Generate the function XML elements
		Element element = document.createElement("Function");			
		addElement(element,"FunctionId",function.getId());
		addElement(element,"FunctionName", function.getName());
		addElement(element,"FunctionDescription", function.getDescription());
		addElement(element,"FunctionExpression", function.getExpression());
		addElement(element,"FunctionCategory", function.getCategory());
		for(Integer inputType : function.getInputTypes())
			addElement(element,"FunctionInputType", inputType);
		addElement(element,"FunctionOutputType", function.getOutputType());
		
		// Generate the function implementation XML elements
		for(FunctionImp functionImp : functionImps)
		{
			Element functionImpElement = document.createElement("FunctionImp");
			addElement(functionImpElement,"FunctionImpLanguage",functionImp.getLanguage());
			addElement(functionImpElement,"FunctionImpDialect",functionImp.getDialect());
			addElement(functionImpElement,"FunctionImpImplementation",functionImp.getImplementation());
		}
		
		return element;
	}
	
	/** Retrieves the element path for the given element ID */
	static private String getElementPath(Integer elementID, HierarchicalSchemaInfo schemaInfo)
	{
		// Get element path
		ArrayList<SchemaElement> elementPath = null;
		for(ArrayList<SchemaElement> possiblePath : schemaInfo.getPaths(elementID))
			if(elementPath==null || elementPath.size()>possiblePath.size())
				elementPath = possiblePath;
		if(elementPath==null || elementPath.size()==0) return null;

		// Translate path to string
		ArrayList<String> path = new ArrayList<String>();
		for(SchemaElement element : elementPath)
			path.add(schemaInfo.getDisplayName(element.getId()));
		
		// Calculate out a path ID (to help with ambiguous paths)
		int pathID=0;
		ArrayList<Integer> pathIDs = schemaInfo.getPathIDs(path);
		for(pathID=0; pathID<pathIDs.size(); pathID++)
			if(pathIDs.get(pathID).equals(elementID)) break;
		
		// Generate the path string
		StringBuffer pathString = new StringBuffer();
		for(String item : path)
			pathString.append("/" + item.replaceAll("/","&#47;"));
		pathString.append(":"+pathID);
		return pathString.toString();
	}	
	
	/** Generates the XML for the specified mapping cell */
	static public Element generate(MappingCell mappingCell, HierarchicalSchemaInfo sourceInfo, HierarchicalSchemaInfo targetInfo, Document documentIn)
	{
		document = documentIn;

		// Retrieve the input and output paths
		ArrayList<String> inputPaths = new ArrayList<String>();
		for(MappingCellInput input : mappingCell.getInputs())
		{
			if(input.getElementID()!=null) 
			{
				String inputPath = getElementPath(input.getElementID(),sourceInfo);
				if(inputPath==null) return null;
				inputPaths.add(inputPath);
			}
			else inputPaths.add("");
		}
		String outputPath = getElementPath(mappingCell.getOutput(),targetInfo);
		if(outputPath==null) return null;

		// Generate the mapping cell XML elements
		Element element = document.createElement("MappingCell");
		addElement(element,"MappingCellId",mappingCell.getId());
		addElement(element,"MappingCellAuthor",mappingCell.getAuthor());
		addElement(element,"MappingCellDate",DateFormat.getDateInstance(DateFormat.MEDIUM).format(mappingCell.getModificationDate()));
		addElement(element,"MappingCellOutputId",mappingCell.getOutput());
		addElement(element,"MappingCellOutputPath",outputPath);
		addElement(element,"MappingCellScore",mappingCell.getScore());
		addElement(element,"MappingCellFunctionId",mappingCell.getFunctionID());
		addElement(element,"MappingCellNotes",mappingCell.getNotes());			
		
		// Generate the mapping cell inputs XML elements
		for(int i=0; i<mappingCell.getInputs().length; i++)
		{
			Element inputElement = addElement(element,"MappingCellInput",null);
			addElement(inputElement,"MappingCellInputId",mappingCell.getInputs()[i]);
			addElement(inputElement,"MappingCellInputPath",inputPaths.get(i));				
		}
		
		return element;
	}
}