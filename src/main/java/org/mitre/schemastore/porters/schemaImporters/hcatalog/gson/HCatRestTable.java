/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.mitre.schemastore.porters.schemaImporters.hcatalog.WebHCatClient;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogParseException;
import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.HCatalogRequestException;




/**
 *
 * @author mgreer
 */
public class HCatRestTable extends HiveDataType {
    private List<HCatRestColumn> columns;
    private String database;
    private String table;
    private Boolean partitioned;
    private String location;
    private String outputFormat;
    private String owner;
    private List<HCatRestColumn> partitionColumns;
    private String inputFormat;
    private Long minFileSize;
    private Long totalNumberFiles;
    private Long lastAccessTime;
    private Long lastUpdateTime;
    private Long maxFileSize;
    private Long totalFileSize;
    private String group;
    private String permission;
    private transient WebHCatClient client;
    private transient Map<String,String> properties;
    private String comment;
    private transient boolean isExtended = false;
    public HCatRestTable() {
    	super(DataType.TABLE);
    }

    public List<HCatRestColumn> getColumns() {
        return columns;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }
    public Boolean getPartitioned() {
        return partitioned;
    }
    public void isExtended(boolean extended) {
    	isExtended = extended;
    }

    public String getLocation() {
        return location;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getOwner() {
        return owner;
    }

    public List<HCatRestColumn> getPartitionColumns() {
        return partitionColumns;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public Long getMinFileSize() {
        return minFileSize;
    }

    public Long getTotalNumberFiles() {
        return totalNumberFiles;
    }

    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public Long getTotalFileSize() {
        return totalFileSize;
    }

    public String getGroup() {
        return group;
    }

    public String getPermission() {
        return permission;
    }

    public Map<String, String> getProperties() throws HCatalogRequestException, HCatalogParseException {
        if (properties == null){
            properties = client.getTableProperties(database, table).getProperties();
        }
        return properties;
    }

    public String getComment() throws HCatalogRequestException, HCatalogParseException {
        if (comment == null) {
            if (getProperties() != null) {
                comment = properties.get("comment");
            }
        }
        return comment;
    }
    public void setComment(String comment) {
        
    }
    public List<HCatRestColumn> getAllColumns() {
        if (!isExtended) {
            return columns;
        }
       ArrayList<HCatRestColumn> allColumns = new ArrayList<HCatRestColumn>();
       allColumns.addAll(columns);
       if (partitioned != null && partitioned == Boolean.TRUE) {
    	   allColumns.addAll(partitionColumns);
       }
       return allColumns;
    }

    public void setClient(WebHCatClient client) {
        this.client = client;
    }
    
    @Override
    public String toString()
    {
        String retString = "{ database: " + database + ", table: " + table + ", columns: [";
        boolean first = true;
        for (HCatRestColumn column : getAllColumns()) {
            if (!first) {
                retString += ", ";
            }
            retString += column.toString();
            first = false;
        }
        retString +="]";
        try {
            if (getComment() != null) {
                retString += ", comment:" + comment;
            }
        } catch (HCatalogRequestException ex) {
           retString += "Error making hCatalog request: " + ex.getMessage();
        } catch (HCatalogParseException ex) {
        	retString += "Error parsing hCatalog tables: " + ex.getMessage();
        }
        retString +="}"; 
        return retString;
        
                
    }
    
}
