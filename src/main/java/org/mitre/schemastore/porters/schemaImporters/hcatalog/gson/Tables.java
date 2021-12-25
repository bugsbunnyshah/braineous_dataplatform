/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

import java.util.List;

/**
 *
 * @author mgreer
 */
public class Tables {
    private String database;
    private List<String> tables;
    
    public Tables() {
        super();
    }
    public String getDatabase() {
        return database;
    }
    public List<String> getTables() {
        return tables;
    }
    
    
    
}
