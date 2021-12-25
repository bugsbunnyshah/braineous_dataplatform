/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

import java.util.Map;

/**
 *
 * @author mgreer
 */
public class HCatRestTableProperties {
   private Map<String, String> properties;
   private String table;
   private String database;
   
   public HCatRestTableProperties() {
       
   }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getTable() {
        return table;
    }

    public String getDatabase() {
        return database;
    }
   
}
