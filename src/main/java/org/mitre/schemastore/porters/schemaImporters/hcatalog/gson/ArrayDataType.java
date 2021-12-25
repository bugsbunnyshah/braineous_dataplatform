/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

/**
 *
 * @author mgreer
 */
public class ArrayDataType extends HiveDataType {
    
    HiveDataType listType;
    
    public ArrayDataType(HiveDataType list) {
        super(DataType.ARRAY);
        listType = list;
    }
    
    public HiveDataType getListType() {
        return listType;
    }
    
}
