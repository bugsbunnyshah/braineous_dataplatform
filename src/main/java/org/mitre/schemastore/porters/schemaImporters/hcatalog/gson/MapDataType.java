/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

/**
 *
 * @author mgreer
 */
public class MapDataType extends HiveDataType {
    HiveDataType key;
    HiveDataType value;
    
    public MapDataType(HiveDataType key, HiveDataType value) {
        super(DataType.MAP);
        this.key = key;
        this.value = value;
    }
    public HiveDataType getKey() {
        return key;
    }
    public HiveDataType getValue() {
        return value;
    }
    
}
