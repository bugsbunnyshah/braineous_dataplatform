package org.mitre.schemastore.porters.schemaExporters.sql;

import java.util.ArrayList;

public class Rdb {

	public static int MAX_NAME_LENGTH = 63; // max for SQL-92

	protected String _rdbName;
	protected boolean _dbSupportsArrayType = false;
	protected boolean _suportsGlobalOid = false;
	protected ArrayList<Table> _relations = new ArrayList<Table>();
	protected ArrayList<RdbAttribute> _attributes = new ArrayList<RdbAttribute>();
	protected ArrayList<View> _views = new ArrayList<View>();
	protected ArrayList<ForeignKey> _foreignKeys = new ArrayList<ForeignKey>();
	protected ArrayList<ViewReference> _viewKeys = new ArrayList<ViewReference>();
	protected ArrayList<ReferenceValue> _refValues = new ArrayList<ReferenceValue>();
	private ArrayList<DomainTable> _referenceTables = new ArrayList<DomainTable>();

	public Rdb(String rdbName) 
		{ _rdbName = rdbName; }

	public String getName() 
		{ return _rdbName; }

	public ArrayList<Table> getRelations() 
		{ return _relations; }

	public ArrayList<View> getViews() 
		{ return _views; }

	public ArrayList<ForeignKey> getForeignKeys() 
		{ return _foreignKeys; }

	public ArrayList<ViewReference> getViewKeys() 
		{ return _viewKeys; }

	public ArrayList<RdbAttribute> getAttributes() 
		{ return _attributes; }

	public ArrayList<ReferenceValue> getReferenceValues() 
		{ return _refValues; }

	/**
	 * Create a relationship in the RDB with a default ID field as primary key
	 * 
	 * @param relationName
	 * @return
	 */
	private Table createRelation(String relationName) {
		if (relationName == null) relationName = getUniqueRelationName();
		Table table = new Table(this, relationName);
		if (!_relations.contains(table)) _relations.add(table);
		return table;
	}

	public Table createTable(String relationName, boolean setDefaultPK) {
		Table rel = createRelation(relationName);
		if (setDefaultPK) createDefaultPK(rel);
		return rel;
	}

	private void createDefaultPK(Table table) 
		{ table.generateDefaultPK(); }

	public View createView(String viewName) {
		View view = new View(this, viewName);
		_views.add(view);
		return view;
	}

	public ForeignKey addForeignKey(Table fromTable, String foreignKey, Table toTable,
			RdbValueType type)  {
		ForeignKey fk = new ForeignKey(this, fromTable, foreignKey, toTable,
				toTable.getPrimaryKey().getName(), type);
		addForeignKey(fk);
		fromTable.addAttribute(fk);
		return fk;
	}

	public ViewReference addViewKey(Table fromTable, String keyName, View refView, RdbValueType type)
			throws NoRelationFoundException, NoRelationFoundException {
		if (refView.getPrimaryKey() == null) {
			System.err.println("throwing NoViewFoundException in Rdb.addViewKey() for " + keyName);
			throw new NoRelationFoundException(refView.getName());
		}
		RdbAttribute toAtt = refView.getPrimaryKey();
		ViewReference vk = new ViewReference(this, fromTable, keyName, refView, toAtt.getName(),
				type);
		_viewKeys.add(vk);
		return vk;
	}

	public ForeignKey addForeignKey(ForeignKey fk) {
		if (!_foreignKeys.contains(fk)) _foreignKeys.add(fk);
		return fk;
	}

	public View addView(String viewName) {
		View view = new View(this, viewName);
		_views.add(view);
		return view;
	}

	private String getUniqueRelationName() {
		return "rel_" + _relations.size();
	}

	public boolean supportsArrayType() {
		return _dbSupportsArrayType;
	}

	public RdbAttribute addAttribute(Table relation, String attName, RdbValueType dbtype,
			boolean isPrimaryKey) {
		
		RdbAttribute attr = new RdbAttribute(this, relation, attName, dbtype);
		addAttribute(relation, attr);
		attr.setIsPrimaryKey(isPrimaryKey);

		return attr;
	}

	public void addAttribute(Table relation, RdbAttribute attribute) {
		relation.addAttribute(attribute);
		if (!_attributes.contains(attribute)) _attributes.add(attribute);
	}

	public void addAttribute(Table relation, ForeignKey fk) {
		addAttribute(relation, fk);
		addForeignKey(fk);
	}

	public void removeAttr(String relationName, String attribute) throws NoRelationFoundException,
			Table.DeletePrimaryKeyException {
		Table rel = getRelation(relationName);
		rel.removeAttribute(attribute);
	}

	public void renameAttribute(String relName, String oldAttName, String newAttName)
			throws NoRelationFoundException, Table.NoAttributeFoundException {
		Table rel = getRelation(relName);
		RdbAttribute att = rel.getAttribute(oldAttName);
		att.setName(newAttName);
	}

	public Table getRelation(String name) throws NoRelationFoundException {
		Table rel;
		for (int i = 0; i < _relations.size(); i++) {
			rel = (Table) _relations.get(i);
			if (rel.getName().equalsIgnoreCase(name)) return rel;
		}

		throw new NoRelationFoundException(name);
	}

	public View getView(String viewName) throws NoRelationFoundException {
		View v;
		for (int i = 0; i < _views.size(); i++) {
			v = (View) _views.get(i);
			if (v.getName().equalsIgnoreCase(viewName)) return v;
		}
		throw new NoRelationFoundException(viewName);
	}

	public RdbAttribute getAttribute(String name) {
		RdbAttribute att;
		for (int i = 0; i < _attributes.size(); i++) {
			att = (RdbAttribute) _attributes.get(i);
			if (att.getName().equalsIgnoreCase(name)) return att;
		}
		return null;
	}

	public void addTable(DomainTable domainTable) {
		if (!_relations.contains(domainTable)) _relations.add(domainTable);
	}

	public DomainTable createDomainTable(String name, boolean setDefaultPK) {
		// DBURDICK: Added "TABLE_" prefix to Table created to store Domain Values
		DomainTable refTbl = new DomainTable(this, "TABLE_" + name);
		addDomainTable(refTbl);
		if (setDefaultPK) createDefaultPK(refTbl);
		try {
			addAttribute(refTbl, new RdbAttribute(this, refTbl, "value", RdbValueType.VARCHAR255,
					false, null));
		} catch (NoRelationFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return refTbl;
	}

	private void addDomainTable(DomainTable refTbl) {
		if (!_referenceTables.contains(refTbl)) _referenceTables.add(refTbl);
		addTable(refTbl);
	}

	public ArrayList<DomainTable> getReferenceTables() {
		return _referenceTables;
	}

}