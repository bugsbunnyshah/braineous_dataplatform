package org.mitre.schemastore.porters.schemaExporters.sql;


import java.util.ArrayList;


public class View extends Table
{
    private ArrayList<Table> _unionTables = new ArrayList<Table>(); // <Relation>:aggregate of
    // relations
    private Table _concreteTable = null; // view's concrete class table,
    // default is abstract class


    public View(Rdb rdb, String name) {
        super( rdb, name );
    }

    public void setConcreteClassTable( Table table ) {
        _concreteTable = table;
        if ( table.getPrimaryKey() != null )
        	super.setPrimaryKey( table.getPrimaryKey() );
        addToUnionRelation( table );
    }
    
    public RdbAttribute getPrimaryKey()  {
    	RdbAttribute pk = super.getPrimaryKey();
    	try {
			pk = super.getAttribute( Table.DEFAULT_PRIMARY_KEY );
		}
		catch (NoAttributeFoundException e) {
			// pk = _rdb.addAttribute( this, Relation.DEFAULT_PRIMARY_KEY,  RdbValueType.INTEGER, true ); @TODO
			System.err.println( "add primary key for " + getName() );
		}
    	return pk;
    }
    
    public Table getConcreteClassTable() {
        return _concreteTable;
    }

    public void addToUnionRelation( Table table ) {
        if ( !_unionTables.contains( table ) )
            _unionTables.add( table );
    }

    public ArrayList<Table> getUnionRelations() {
        return _unionTables;
    }
    
   

}