package org.mitre.schemastore.porters.schemaExporters.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class RdbAttribute {

	private ArrayList<String> _allowedValues = new ArrayList<String>();
	private String _comment = "";
	private String _databaseParam;
	private boolean _isUnique = false;
	private String _viewWidget;
	private String _viewWidgetParameter;
	private String _widget;
	private String _widgetParameter;
	protected String _defaultValue = null;
	protected RdbAttribute _inverseAttribute; // TODO implement
	protected boolean _isPrimaryKey = false; // TODO remove foreign key as a
	// type from databaseType
	protected boolean _isRequired = false;
	protected Number _maximumValue;
	protected Number _minimumValue;
	protected String _name;
	protected Rdb _rdb;
	protected Table _relation = null;
	protected String _relName = null;
	protected RdbValueType _type;
	protected Integer _valueLength = null;
	protected boolean _isAssociated = true;
	protected Float _viewSequence;

	public RdbAttribute(Rdb schema, Table rel, String attributeName, RdbValueType type) {
		_type = type;
		_name = attributeName;
		_rdb = schema;
		_relation = rel;
		_relName = rel.getName();
		_rdb.addAttribute(rel, this);

		if (_type.equals(RdbValueType.ID))  {
			_type = RdbValueType.AUTO_INCREMENT;
			rel.setPrimaryKey(this); 
		} 
	}

	public RdbAttribute(Rdb schema, Table containerRelation, String attributeName,
                        RdbValueType type, boolean isRequired, String defaultValue)
			throws NoRelationFoundException {
		this(schema, containerRelation.getName(), attributeName, type);
		setIsRequired(isRequired);
		setDefaultValue(defaultValue);
	}

	public RdbAttribute(Rdb schema, String containerRelName, String attributeName, RdbValueType type)
			throws NoRelationFoundException {
		_name = attributeName;
		_type = type;
		_rdb = schema;
		if (containerRelName != null) {
			_relation = _rdb.getRelation(containerRelName);
			_relName = containerRelName;
		}

	}

	public RdbAttribute(Table containerTbl, String attributeName, RdbValueType type,
                        boolean isRequired, String defaultValue) {
		if (containerTbl != null) {
			_relName = containerTbl.getName();
			_relation = containerTbl;
		}
		_name = attributeName;
		_rdb = null;
		_type = type;
		_isRequired = isRequired;
		_defaultValue = defaultValue;
	}

	public String toString(){
		return _name;
	}
	
	public int compareTo(Object anotherAtt) {
		return getName().compareTo(((RdbAttribute) anotherAtt).getName());
	}

	public boolean equals(Object anotherAtt) {
		return getName().equals(((RdbAttribute) anotherAtt).getName());
	}

	public Collection<String> getAllowedValues() {
		if (_allowedValues.size() > 0) return _allowedValues;
		else return null;
	}

	public String getComment() {
		return (_comment != null && _comment.length() != 0) ? ("-- " + _comment) : "";
	}

	public String getContainerRelationName() {
		return _relName;
	}

	public String getDatabaseParameter() {
		return (_databaseParam == null) ? "" : _databaseParam;
	}

	public RdbValueType getRdbValueType() {
		return _type;
	}

	public Integer getRdbValueLength() {
		Integer length = null;
		if (_type.equals("" /* RdbValueType.VARCHAR */)) { // @TODO
			String dbParam = getDatabaseParameter();
			if (dbParam != null && dbParam.length() > 0) length = Integer.decode(dbParam);
		}
		return length;
	}

	public String getDatabaseTypeString() {
		String s = _type.toString();
		if (_type.equals(RdbValueType.FOREIGN_KEY)) s = RdbValueType.INTEGER.toString();
		else if (_type.equals(RdbValueType.ID)) s = RdbValueType.AUTO_INCREMENT.toString() + " "
				+ "PRIMARY KEY ";

		return s;
	}

	public String getDefaultValue() {
		return _defaultValue;
	}

	public RdbAttribute getInverseAttribute() {
		return _inverseAttribute;
	}

	public Number getMaximumValue() {
		return _maximumValue;
	}

	public Number getMinimumValue() {
		return _minimumValue;
	}

	public String getName() {
		return _name;
	}

	public Table getRelation() {
		return _relation;
	}

	public String getViewWidget() {
		return _viewWidget;
	}

	public String getViewWidgetParameter() {
		return _viewWidgetParameter;
	}

	public String getWidget() {
		return _widget;
	}

	public String getWidgetParameter() {
		return _widgetParameter;
	}

	public Float getViewSequence() {
		return _viewSequence;
	}

	public int hashCode() {
		return (31 + getName().hashCode());
	}

	// @TODO
	// public boolean isForeignKey() {
	// return _type.equals( /* RdbValueType.FOREIGN_KEY */ );
	// }

	public boolean isPrimaryKey() {
		return _isPrimaryKey;
	}

	public boolean isRequired() {
		return _isRequired;
	}

	public boolean isUnique() {
		return _isUnique;
	}

	public boolean isAssociated() {
		return _isAssociated;
	}

	/**
	 * sets the allowed values for symbol or varchar type only. (Instance or Class types are done
	 * with other methods)
	 * 
	 * @param allowedValues
	 */
	public void setAllowedValues(Collection<String> allowedValues) {
		for (Iterator<String> avIter = allowedValues.iterator(); avIter.hasNext();)
			_allowedValues.add(avIter.next());
	}

	public void setComment(String c) {
		_comment = c;
	}

	public void setContainerRelation(Table relation) {
		_relation = relation;
		_relName = relation.getName();

	}

	public void setDatabaseParameter(int i) {
		_databaseParam = String.valueOf(i);
	}

	public void setDatabaseTypeParameter(String param) {
		_databaseParam = param;
	}

	public void setDatabaseType(RdbValueType type) {
		_type = type;
	}

	public void setDefaultValue(String val) {
		_defaultValue = val;
	}

	public void setFormWidget(String widget) {
		_widget = widget;
	}

	public void setFormWidgetParameter(String param) {
		_widgetParameter = param;
	}

	public void setInverse(RdbAttribute a) {
		_inverseAttribute = a;
	}

	public void setIsPrimaryKey(boolean primaryKey) {
		_isPrimaryKey = primaryKey;
	}

	public void setIsRequired(boolean notNull) {
		_isRequired = notNull;
	}

	public void setIsUnique(boolean b) {
		_isUnique = b;
	}

	public void setMaximumValue(Number max) {
		_maximumValue = max;
	}

	public void setMinimumValue(Number min) {
		_minimumValue = min;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setViewWidget(String formWidget) {
		_viewWidget = formWidget;
	}

	public void setViewWidgetParameter(String viewParam) {
		_viewWidgetParameter = viewParam;
	}

	public void setIsAssociated(boolean assoc) {
		_isAssociated = assoc;
	}

	public void setViewSequence(Float viewSequence) {
		_viewSequence = viewSequence;
	}
}
