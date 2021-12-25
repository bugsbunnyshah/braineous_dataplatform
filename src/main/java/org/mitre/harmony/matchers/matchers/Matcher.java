// (c) The MITRE Corporation 2006
// ALL RIGHTS RESERVED
package org.mitre.harmony.matchers.matchers;

import java.util.ArrayList;
import java.util.HashMap;

import org.mitre.harmony.matchers.MatchTypeMappings;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.parameters.MatcherParameter;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;

/** Matcher Interface - A matcher scores source-target linkages based on a specific algorithm */	
public abstract class Matcher
{
	// Constants for the major parameter names
	protected static final String NAME = "UseName";
	protected static final String DESCRIPTION = "UseDescription";
	protected static final String THESAURUS = "UseThesaurus";
	protected static final String TRANSLATE = "UseTranslator";
	protected static final String HIERARCHY = "UseHierarchy";
	protected static final String IGNORECASE = "IgnoreCase";
	protected static final String MATCHTYPE = "MatchType";
	
	// Stores the match merger schema information
	protected FilteredSchemaInfo schema1, schema2;

	/** Stores the match merger type mapping information */
	private MatchTypeMappings types;

	/** Stores the parameter defaults */
	private HashMap<String,String> defaults = new HashMap<String,String>();
	
	/** Stores if this is a default matcher */
	private boolean isDefaultMatcher = false;

	/** Stores if this is a hidden matcher */
	private boolean isHiddenMatcher = false;
	
	// Stores the completed and total number of comparisons that need to be performed
	protected int completedComparisons = 0, totalComparisons = 1;
	
	/** Return the name of the matcher */
	abstract public String getName();

	/** Indicates if the matcher needs a repository client */
	public boolean needsClient() { return false; }
	
	/** Returns the list of parameters associated with the matcher */
	protected ArrayList<MatcherParameter> getMatcherParameters() { return new ArrayList<MatcherParameter>(); }
	
	// Matcher getters
	final public boolean isDefault() { return isDefaultMatcher; }
	final public boolean isHidden() { return isHiddenMatcher; }

	// Matcher setters
	final public void setDefault(boolean isDefault) { this.isDefaultMatcher = isDefault; }
	final public void setHidden(boolean isHidden) { this.isHiddenMatcher = isHidden; }

	/** Initializes the matcher */
	final public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2)
		{ this.schema1 = schema1; this.schema2 = schema2; this.types = null; }

	/** Initializes the matcher */
	final public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2, MatchTypeMappings types)
		{ this.schema1 = schema1; this.schema2 = schema2; this.types = types; }

	/** Sets the parameter default */
	final public void setDefault(String name, String value)
		{ defaults.put(name,value); }

	/** Retrieve the matcher parameters */
	final public ArrayList<MatcherParameter> getParameters()
	{
		ArrayList<MatcherParameter> parameters = new ArrayList<MatcherParameter>();
		for(MatcherParameter parameter : getMatcherParameters())
		{
			String value = defaults.get(parameter.getName());
			if(value!=null) parameter.setValue(value);
			parameters.add(parameter);
		}
		return parameters;
	}
	
	/** Generates scores for the specified graphs */
	abstract public MatcherScores match();

	/** Indicates if the specified elements can validly be mapped together */
	final protected boolean isAllowableMatch(SchemaElement element1, SchemaElement element2)
		{ return types == null || types.isMapped(element1, element2); }

	/** Indicates the completion percentage of the matcher */
	final public double getPercentComplete()
		{ return 1.0 * completedComparisons / totalComparisons; }
}