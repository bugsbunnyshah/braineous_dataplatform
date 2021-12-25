package org.mitre.harmony.matchers.parameters;

public abstract class MatcherParameter
{
	// Stores the matcher parameter variables
	private String name;
	private String text;
	private String value;

	/** Constructs the matcher parameter */
	public MatcherParameter(String name, String value)
	{
		this.name=name; this.value=value;
		this.text=name.replaceAll("([a-z])([A-Z])","$1 $2");
	}
	
	/** Constructs the matcher parameter */
	public MatcherParameter(String name, String text, String value)
		{ this.name=name; this.text=text; this.value=value; }

	// Getters for this class
	public String getName() { return name; }
	public String getText() { return text; }
	public String getValue() { return value; }

	/** Sets the matcher parameter value */
	public void setValue(String value)
		{ this.value = value; }
}