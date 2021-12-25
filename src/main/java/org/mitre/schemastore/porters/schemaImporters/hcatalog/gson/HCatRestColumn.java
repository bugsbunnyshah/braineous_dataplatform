/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;

import org.mitre.schemastore.porters.schemaImporters.hcatalog.parser.Parser;

import de.susebox.jtopas.TokenizerException;




/**
 *
 * @author mgreer
 */
public class HCatRestColumn {
    private String name;
    private String type;
    private String comment;
    private transient HiveDataType hiveType;
    private static Parser parser = null;
    
    
    
    
    public HCatRestColumn() {
        super();
    }
    
    public String getName() {
        return name;
    }
    String getType() { 
        return type;
    }
    public String getComment() {
    	if (comment != null && !comment.isEmpty() && comment.equals("from deserializer")){
    		comment = "";
    	}
        return comment;
    }
    public HiveDataType getHiveType() throws TokenizerException {
        if (hiveType == null && type != null) {
            if (parser == null) {
                parser = new Parser(type);
            }
            else
            {
                parser.setTypeString(type);
            }
            hiveType = parser.parse();
        }
        return hiveType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setHiveType(HiveDataType hiveType) {
        this.hiveType = hiveType;
    }
    
    public String toString() {
        return "{ name: " + name + ", type:" + type +(comment==null?"":", comment: "+comment) + "}";
    }


    
}
