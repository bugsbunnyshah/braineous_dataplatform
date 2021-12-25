package org.mitre.schemastore.porters.schemaExporters.sql;

import java.util.ArrayList;

public class Table
{
    public static String DEFAULT_PRIMARY_KEY = "ID";
    protected Rdb _rdb;
    protected String _name;
    protected ArrayList<RdbAttribute> _attributes = new ArrayList<RdbAttribute>();
    protected RdbAttribute _primaryKey = null ; // TODO expand to composite Primary Key
    protected ArrayList<RdbAttribute> _primaryKeySet = new ArrayList<RdbAttribute>();    
    protected boolean _hasAttributes = false;
    protected String _comment = "";

    public Table(Rdb db, String name) {
        _rdb = db;
        _name = name;
    }

    public String toString(){
    	return _name;
    }

    public void setPrimaryKey( RdbAttribute att ) {
        _primaryKey = att;
        _primaryKeySet.add( att );
        att.setIsPrimaryKey( true );
        if ( !_attributes.contains( att ) )
            _attributes.add( _primaryKey );
    }

    public RdbAttribute getPrimaryKey() {
    	if ( _primaryKey == null )
    		generateDefaultPK();
        return _primaryKey;
    }
    
    public ArrayList<RdbAttribute> getPrimaryKeySet() {
    	return _primaryKeySet;
    }

    public String getName() {
        return _name;
    }

    public void setIsVisible( boolean v ) {
        _hasAttributes = v;
    }

    /**
     * Currently only checks to make sure the table has more than an ID column.
     * May add later.
     * 
     * @return
     */
    public boolean isValid() {
        return _hasAttributes;
    }

    public void addAttribute( RdbAttribute a ) {
        if ( !_attributes.contains( a ) )
            _attributes.add( a );
        if ( !_hasAttributes )
            _hasAttributes = true;
        if (a.getContainerRelationName() == null )
        	a.setContainerRelation(this);
    }

    public ArrayList<RdbAttribute> getAttributes() {
        return _attributes;
    }

    public void removeAttribute( String attName )
            throws DeletePrimaryKeyException {
        try {
            if ( attName.compareTo( _primaryKey.getName() ) == 0 )
                throw new DeletePrimaryKeyException();

            RdbAttribute att = getAttribute( attName );
            _attributes.remove( att );

            if ( _attributes.size() <= 1 )
                _hasAttributes = false;
        }
        catch (NoAttributeFoundException e) {
            System.out.println( e.getMessage() );
            System.out.println( "Attribute " + attName
                    + " is not found during removal" );
        }
    }

    public RdbAttribute getAttribute( String attName )
            throws NoAttributeFoundException {
        RdbAttribute att;
        for ( int i = 0; i < _attributes.size(); i++ ) {

            att = (RdbAttribute)_attributes.get( i );
            if ( att.getName().compareTo( attName ) == 0 )
                return att;
        }
        throw new NoAttributeFoundException();
    }
    

    static public class NoAttributeFoundException extends Exception
    {
        static final long serialVersionUID = 1L;

        public NoAttributeFoundException() {
            super();
        }

        public NoAttributeFoundException(String attributeName) {
            super( "NoAttributeFoundException " + attributeName );
        }
    }

    static public class DeletePrimaryKeyException extends Exception
    {
        static final long serialVersionUID = 1L;

        public DeletePrimaryKeyException() {
            super();
        }

        public DeletePrimaryKeyException(String s) {
            super( "Exception: default primary key not deleted from a table "
                    + s );
        }
    }
    
    public void setComment (String comment) {
    	_comment = comment;
    }
    
    public String getComment(){
    	return _comment;
    }


	public void generateDefaultPK() {
		createFK(DEFAULT_PRIMARY_KEY); 
	}


	public RdbAttribute createFK(String attributeName) {
		RdbAttribute pk = new RdbAttribute(_rdb, this, attributeName, RdbValueType.AUTO_INCREMENT);
		setPrimaryKey(pk);
		return pk;
	}

}
