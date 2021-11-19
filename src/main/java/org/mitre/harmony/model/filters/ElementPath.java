package org.mitre.harmony.model.filters;

import java.util.ArrayList;

import org.mitre.schemastore.model.SchemaElement;

/** Class for storing an element path */
public class ElementPath
{	
	/** Stores the list of elements which make up the element path */
	private ArrayList<Integer> elementPath = null;
	
	/** Constructs the element path*/
	public ElementPath(ArrayList<Integer> elementPath)
		{ this.elementPath = elementPath; }

	/** Returns the element ID */
	public Integer getElementID()
		{ return elementPath.size()==0 ? null : elementPath.get(elementPath.size()-1); }

	/** Returns the size of the element path */
	public Integer size()
		{ return elementPath.size(); }
	
	/** Indicates if this path contains the specified path */
	public boolean contains(ElementPath elementPath)
	{
		if(elementPath.elementPath.size()>this.elementPath.size()) return false;
		for(int i=0; i<elementPath.elementPath.size(); i++)
			if(!elementPath.elementPath.get(i).equals(this.elementPath.get(i))) return false;
		return true;
	}
	
	/** Indicates if two element paths are equal to one another */
	public boolean equals(Object obj)
	{
		if(!(obj instanceof ElementPath)) return false;
		return elementPath.equals(((ElementPath)obj).elementPath);
	}
	
	/** Generates an element path for the array of schema elements */
	static public ElementPath getElementPath(ArrayList<SchemaElement> elements)
	{
		ArrayList<Integer> elementPath = new ArrayList<Integer>();
		for(SchemaElement element : elements)
			elementPath.add(element.getId());
		return new ElementPath(elementPath);
	}
}