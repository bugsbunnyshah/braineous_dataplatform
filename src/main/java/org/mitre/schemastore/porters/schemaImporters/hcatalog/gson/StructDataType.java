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
public class StructDataType extends HiveDataType {
    private List<HCatRestColumn> columns;
    
    public StructDataType(List<HCatRestColumn> columns) {
        super(DataType.STRUCT);
        this.columns = columns;
        
    }
    public List<HCatRestColumn> getColumns () {
        return Collections.unmodifiableList(columns);
    }
}
