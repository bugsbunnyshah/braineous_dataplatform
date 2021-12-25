/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

import java.util.ArrayList;
import java.util.List;

import org.mitre.schemastore.porters.schemaImporters.hcatalog.WebHCatClient;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogParseException;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogRequestException;


/**
 *
 * @author mgreer
 */
public class HCatRestDatabase extends HiveDataType {
    private String database;   
    private String location;    
    private String comment;  
    private String params;
    private transient WebHCatClient client;
    private transient List<String> tables;
    private transient List<HCatRestTable> tableSchema;
   
    HCatRestDatabase() { 
    	super(DataType.DATABASE);
    }

    public String getDatabase() {
        return database;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public String getParams() {
        return params;
    }
    
    public void setClient(WebHCatClient client) {
        this.client = client;
    }
    public List<String> getTables() throws HCatalogRequestException, HCatalogParseException {
        if (tables == null) {
            tables = client.listTableNamesByPattern(database, null);
        }
        return tables;
    }
    public List<HCatRestTable> getTableSchemas() throws HCatalogRequestException, HCatalogParseException {
        if (tableSchema == null) {
            tableSchema = new ArrayList<HCatRestTable>();
            for (String tablename : getTables()) {
                tableSchema.add(client.getTable(database, tablename));
            }
        }
        return tableSchema;
    }
    public String toString() {
 
            String retString = "{ database: " + database + ", tables: [";
            boolean first = true;
         try {
            for (HCatRestTable table : getTableSchemas()) {
                if (!first) {
                    retString += ", ";
                }
                retString += table.toString();
                first = false;
            }
            retString += "]" + (comment == null?"":", comment: " + comment) + "}";

        } catch (HCatalogRequestException ex) {
            return "Error getting database schema: " + ex.getMessage();
        } catch (HCatalogParseException ex) {
        	return "Error parsing database schema: " + ex.getMessage();
        }
         return retString;
       
    }
}
