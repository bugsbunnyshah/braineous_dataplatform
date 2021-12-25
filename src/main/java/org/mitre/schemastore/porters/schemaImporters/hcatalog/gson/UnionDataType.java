/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author mgreer
 */
public class UnionDataType extends HiveDataType {
    private List<HiveDataType> dataTypes;
    
    public UnionDataType(List<HiveDataType> dataTypes) {
        super(DataType.UNION);
        this.dataTypes = dataTypes;
        
    }
    public List<HiveDataType> getDataTypes() {
        return Collections.unmodifiableList(dataTypes);
    }
}
