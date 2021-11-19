// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.schemaInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Synonym;
import org.mitre.schemastore.model.Subtype;

import org.mitre.schemastore.model.schemaInfo.model.*;

/**
 * Class for generating hierarchy in the schema info
 */
public class HierarchicalSchemaInfo extends SchemaInfo
{
	// Pattern used to extract schema models
	static private Pattern schemaModelPattern = Pattern.compile("<schemaModel>\\s*<class>\\s*(.*?\\S)?\\s*</class>\\s*(<name>\\s*(.*?\\S)?\\s*</name>)?\\s*</schemaModel>");


	/** Stores the list of available schema models */
	static private ArrayList<SchemaModel> schemaModels = null;

	/** Stores the current model being used to interpret the schema info */
	private SchemaModel model;
	public static void main(String[] args){
		initSchemaModels();
		System.out.println(schemaModels.size());
		for (SchemaModel model : schemaModels) {
			System.out.println(model.getClass().getSimpleName() + " " + model.getName());
		}
	}
	/** Initializes the schema models */
	static private void initSchemaModels()
	{
		schemaModels = new ArrayList<SchemaModel>();

		// Retrieve schema models from file
		try {
			// Pull the entire file into a string
			InputStream configStream = HierarchicalSchemaInfo.class.getResourceAsStream("/schemamodels.xml");
			BufferedReader in = new BufferedReader(new InputStreamReader(configStream));
			StringBuffer buffer = new StringBuffer("");
			String line; while((line=in.readLine())!=null) buffer.append(line);
			in.close();

			// Parse out the schema models
			Matcher schemaModelMatcher = schemaModelPattern.matcher(buffer);
			while(schemaModelMatcher.find())
				try {
					SchemaModel schemaModel = (SchemaModel)Class.forName(schemaModelMatcher.group(1)).newInstance();
					if (schemaModelMatcher.group(3) != null)
					{
						schemaModel.setName(schemaModelMatcher.group(3));
					}
					schemaModels.add(schemaModel);
				} catch(Exception e) { System.out.println("HierarchicalSchemaInfo.initSchemaModels():  " + e.getMessage()); }
		}
		catch(IOException e)
			{ System.out.println("(E)HierarchicalSchemaInfo - schemamodels.xml has failed to load!\n"+e.getMessage()); }
	}

	/** Constructs the hierarchical schema with the default model */
	public HierarchicalSchemaInfo(SchemaInfo schemaInfo)
		{ super(schemaInfo); setModel(null); }
	
	/** Constructs the hierarchical schema with the specified model */
	public HierarchicalSchemaInfo(SchemaInfo schemaInfo, SchemaModel model)
		{ super(schemaInfo); setModel(model); }

	/** Get the available list of schema models */
	static public ArrayList<SchemaModel> getSchemaModels()
	{
		if(schemaModels==null) initSchemaModels();
		return schemaModels;
	}

	/** Get the current schema model */
	public SchemaModel getModel()
		{ return model; }

	/** Sets the schema model */
	public void setModel(SchemaModel model)
	{
		// If no model given, automatically determine the default schema model
		if(model==null)
		{
			// Determine the makeup of the schema
			Integer totalCount=0, entityCount=0, domainCount=0, containmentCount=0, synonymCount=0, relationshipCount=0, subtypeCount=0;
			HashSet<Integer> synonymEntities = new HashSet<Integer>();
			for(SchemaElement element : getElements(null))
			{
				//if (element instanceof Domain || element instanceof DomainValue ||
				//element instanceof Containment || element instanceof Entity ||
				// element instanceofSynonym {
				if(element instanceof Domain || element instanceof DomainValue) domainCount++;
				if(element instanceof Containment && ((Containment)element).getName().length()>0) containmentCount++;
				if(element instanceof Entity) entityCount++;
				if(element instanceof Synonym) { synonymCount++; synonymEntities.add(((Synonym)element).getElementID());}
				if (element instanceof Relationship) relationshipCount++;
				if (element instanceof Subtype) subtypeCount++;
				totalCount++;
				//}
			}

			// Identify which model to use
			if(domainCount.equals(totalCount)) model = new DomainSchemaModel();
			else if(synonymCount+synonymEntities.size()==totalCount) model = new SynonymModel();
			else if (subtypeCount>0 && relationshipCount>0) model = new OWLSchemaModel();
			else if(containmentCount>0 && (containmentCount<1000 || containmentCount/entityCount<3))  {
				model = new XMLSchemaModel();
				ArrayList root = model.getRootElements(this);
				if (root == null || root.size()==0) {
					model = new ContainingRelationshipSchemaModel();
				}
			}
			else model = new RelationalSchemaModel();
		}
		this.model = model;
	}

	/** Returns the root elements in this schema */
	public ArrayList<SchemaElement> getRootElements()
		{ return model.getRootElements(this); }

	/** Returns the parent elements of the specified element in this schema */
	public ArrayList<SchemaElement> getParentElements(Integer elementID)
		{ return model.getParentElements(this,elementID); }

	/** Returns the children elements of the specified element in this schema */
	public ArrayList<SchemaElement> getChildElements(Integer elementID)
		{ return model.getChildElements(this,elementID); }

	/** Adds children to the schema tree */
	public ArrayList<SchemaElement> getChildElements(Integer elementID, HashSet<Integer> pathIDs)
	{
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		ArrayList<SchemaElement> baseChildElements = getChildElements(elementID);
		if (baseChildElements != null){
		for(SchemaElement childElement : baseChildElements)
		{
			// Don't add children if element already in branch
			if(pathIDs.contains(childElement.getId())) continue;

			// Don't add children if type already in branch
			SchemaElement type = getModel().getType(this, childElement.getId());
			if(type!=null)
			{
				boolean duplicatedType = false;
				for(Integer pathID : pathIDs)
					if(type.equals(getModel().getType(this, pathID)))
						{ duplicatedType = true; break; }
				if(duplicatedType) continue;
			}

			// Adds the child element to the list of children elements
			if (!childElements.contains(childElement)){
				childElements.add(childElement);
			}
		}
		}
		// Retrieve child elements from the hierarchical schema info
		return childElements;
	}


	/** Returns the ancestors of the specified element in this schema */
	private void getAncestorElements(Integer elementID, HashSet<SchemaElement> ancestors)
	{
		for(SchemaElement parentElement : getParentElements(elementID))
			if(!ancestors.contains(parentElement))
			{
				ancestors.add(parentElement);
				getAncestorElements(parentElement.getId(),ancestors);
			}
	}

	/** Returns the ancestors of the specified element in this schema */
	public ArrayList<SchemaElement> getAncestorElements(Integer elementID)
	{
		HashSet<SchemaElement> ancestors = new HashSet<SchemaElement>();
		getAncestorElements(elementID,ancestors);
		return new ArrayList<SchemaElement>(ancestors);
	}

	/** Returns the descendants of the specified element in this schema */
	private void getDescendantElements(Integer elementID, HashSet<SchemaElement> descendants)
	{
		for(SchemaElement childElement : getChildElements(elementID))
			if(!descendants.contains(childElement))
			{
				descendants.add(childElement);
				getDescendantElements(childElement.getId(),descendants);
			}
	}

	/** Returns the descendants of the specified element in this schema */
	public ArrayList<SchemaElement> getDescendantElements(Integer elementID)
	{
		HashSet<SchemaElement> descendants = new HashSet<SchemaElement>();
		getDescendantElements(elementID,descendants);
		return new ArrayList<SchemaElement>(descendants);
	}

	/** Returns the domain of the specified element in this schema */
	public Domain getDomainForElement(Integer elementID)
		{ return model.getDomainForElement(this,elementID); }

	/** Returns the elements referenced by the specified domain */
	public ArrayList<SchemaElement> getElementsForDomain(Integer domainID)
		{ return model.getElementsForDomain(this,domainID); }

	/** Returns the domain values associated with the specified element in this schema */
	public ArrayList<DomainValue> getDomainValuesForElement(Integer elementID)
	{
		ArrayList<DomainValue> domainValues = new ArrayList<DomainValue>();
		Domain domain = getDomainForElement(elementID);
		if(domain!=null)
			domainValues.addAll(getDomainValuesForDomain(domain.getId()));
		return domainValues;
	}

	/** Returns the type name associated with the specified element (or NULL if element has no name) */
	public SchemaElement getType(HierarchicalSchemaInfo schemaInfo, Integer elementID)
		{ return model.getType(schemaInfo, elementID); }
	
	/** Returns the type display (allows overriding of the type) */
	public String getTypeString(HierarchicalSchemaInfo schemaInfo, Integer elementID)
		{ return model.getTypeString(schemaInfo, elementID); }
	
	/** Returns the list of all elements in this schema */
	public ArrayList<SchemaElement> getHierarchicalElements()
	{
		// Construct the list of root elements
		HashSet<Integer> usedElements = new HashSet<Integer>();
		ArrayList<SchemaElement> elements = new ArrayList<SchemaElement>(getRootElements());
		for(SchemaElement element : elements)
			usedElements.add(element.getId());

		// Follow all paths in the hierarchy
		for(int i=0; i<elements.size(); i++)
			for(SchemaElement childElement : getChildElements(elements.get(i).getId()))
				if(!usedElements.contains(childElement.getId()))
				{
					elements.add(childElement);
					usedElements.add(childElement.getId());
				}

		// Return the list of all elements in the hierarchy
		return elements;
	}

	/** Returns the paths from the root element to the partially built path */
	private ArrayList<ArrayList<SchemaElement>> getPaths(ArrayList<SchemaElement> partialPath)
	{
ArrayList<ArrayList<SchemaElement>> paths = new ArrayList<ArrayList<SchemaElement>>();
		
		Integer pID = null;

		if (partialPath != null) {

			if (partialPath.get(0) != null) {
				
				if (partialPath.get(0).getId() != null) {
					pID = partialPath.get(0).getId();
				}
			}
		}

		ArrayList<SchemaElement> parentElements = getParentElements(pID);

		// Handles case where root element has been reached
		if(parentElements.size()==0)
			paths.add(new ArrayList<SchemaElement>(partialPath));

		// Handles case where root element is still deeper
		for(SchemaElement element : parentElements)
			if(!partialPath.contains(element))
			{
				partialPath.add(0,element);
				paths.addAll(getPaths(partialPath));
				partialPath.remove(0);
			}
		return paths;
	}

	/** Returns the various paths from the root element to the specified element */
	public ArrayList<ArrayList<SchemaElement>> getPaths(Integer elementID)
	{
		// Don't proceed if element doesn't exist in the schema
		if(getElement(elementID)==null)
			return new ArrayList<ArrayList<SchemaElement>>();

		// Retrieve all of the paths associated with the specified schema element
		ArrayList<SchemaElement> partialPath = new ArrayList<SchemaElement>();
		partialPath.add(getElement(elementID));
		return getPaths(partialPath);
	}

	/** Returns the various IDs associated with the specified partial path */
	private ArrayList<Integer> getPathIDs(Integer elementID, List<String> path)
	{
		ArrayList<Integer> pathIDs = new ArrayList<Integer>();

		// Get name information
		String elementName = getElement(elementID).getName();
		String displayedName = getDisplayName(elementID);
		String pathName = path.get(0);

		// Check to see if element in partial path
		if(elementName.equalsIgnoreCase(pathName) || displayedName.equalsIgnoreCase(pathName))
		{
			if(path.size()>1)
			{
				List<String> subPath = path.subList(1, path.size());
				for(SchemaElement element : getChildElements(elementID))
					pathIDs.addAll(getPathIDs(element.getId(), subPath));
			}
			else pathIDs.add(elementID);
		}

		return pathIDs;
	}

	/** Returns the various IDs associated with the specified path */
	public ArrayList<Integer> getPathIDs(ArrayList<String> path)
	{
		ArrayList<Integer> pathIDs = new ArrayList<Integer>();
		for(SchemaElement element : getRootElements())
			pathIDs.addAll(getPathIDs(element.getId(),path));
		return pathIDs;
	}

	/** Returns the various depths of the specified element in the schema */
	public ArrayList<Integer> getDepths(Integer elementID)
	{
		HashSet<Integer> depths = new HashSet<Integer>();
		for(ArrayList<SchemaElement> paths : getPaths(elementID))
			depths.add(paths.size());
		return new ArrayList<Integer>(depths);
	}
}
