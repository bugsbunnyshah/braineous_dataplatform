package org.mitre.schemastore.porters.schemaExporters.sql;


public class ForeignKey extends RdbAttribute
{

    private RdbAttribute _reference;

    public ForeignKey(Rdb schema, Table fromTable, String fromAtt,
                      Table toTable, String toAtt, RdbValueType type)  {
        super( schema, fromTable, fromAtt, type );
        setReferencedAttribute( toTable );
    }

    public RdbAttribute getReferencedAttribute() {
        return _reference;
    }

    public void setReferencedAttribute( Table table ) {
        RdbAttribute toPK = table.getPrimaryKey();
        _reference = toPK;
    }

   

}