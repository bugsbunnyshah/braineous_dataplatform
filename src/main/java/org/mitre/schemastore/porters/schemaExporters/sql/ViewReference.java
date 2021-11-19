package org.mitre.schemastore.porters.schemaExporters.sql;


public class ViewReference extends RdbAttribute
{
	private RdbAttribute _toAtt; // attribute that's referenced in the view
	private View _toView;

	public ViewReference(Rdb schema, Table fromTable, String fromAtt, View refView, String toAtt, RdbValueType type)
			throws NoRelationFoundException {
		super( schema, fromTable, fromAtt, type );
		_toView = refView;
		setReferencedAttribute( refView );
	}

	public RdbAttribute getReferencedAttribute() {
		return _toAtt;
	}

	public void setReferencedAttribute( View view ) {
		RdbAttribute toPK = view.getPrimaryKey();
		if ( toPK != null )
			_toAtt = toPK;
	}
	
	public View getReferencedView() {
		return _toView;
	}

}
