package org.mitre.schemastore.porters.schemaExporters.sql;

import java.util.ArrayList;

public class DomainTable extends Table {

	private ArrayList<ReferenceValue> _values = new ArrayList<ReferenceValue>();

	public DomainTable(Rdb rdb, String name) {
		super(rdb, name);
	}

	public void addDomainValue(String value) {
		ReferenceValue rv = new ReferenceValue(this, value);
		if (!_values.contains(rv))
			_values.add(rv);
	}

	public ArrayList<ReferenceValue> getReferenceValues() {
		return _values;
	}

}
