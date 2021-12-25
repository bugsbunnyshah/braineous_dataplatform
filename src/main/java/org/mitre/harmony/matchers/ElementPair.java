package org.mitre.harmony.matchers;

/** Class for storing a pair of elements */
public class ElementPair
{
	/** Stores source element */
	private Integer sourceElement;
	
	/** Stores target element */
	private Integer targetElement;
	
	/** Constructs the element pair */
	public ElementPair(Integer sourceElement, Integer targetElement)
		{ this.sourceElement = sourceElement; this.targetElement = targetElement; }
	
	/** Returns source element */
	public Integer getSourceElement()
		{ return sourceElement; }
	
	/** Returns target element */
	public Integer getTargetElement()
		{ return targetElement; }

	/** Provides a hash code for the element pair */
	public int hashCode()
		{ return sourceElement*sourceElement + targetElement; }
	
	/** Indicates if two element pairs are equivalent */
	public boolean equals(Object object)
	{
		if(!(object instanceof ElementPair)) return false;
		ElementPair pair = (ElementPair)object;
		return sourceElement.equals(pair.sourceElement) && targetElement.equals(pair.targetElement);
	}
}