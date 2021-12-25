package org.mitre.harmony.matchers.parameters;

/** Class for storing the Matcher Checkbox Parameter */
public class MatcherCheckboxParameter extends MatcherParameter
{
	/** Constructs the matcher checkbox parameter */
	public MatcherCheckboxParameter(String name, Boolean value)
		{ super(name,value.toString()); }
	
	/** Constructs the matcher checkbox parameter */
	public MatcherCheckboxParameter(String name, String text, Boolean value)
		{ super(name,text,value.toString()); }
	
	/** Indicates if the parameter is selected */
	public Boolean isSelected()
		{ return getValue().equals("true"); }

	/** Marks the parameter as selected */
	public void setSelected(Boolean selected)
		{ setValue(selected.toString()); }
}