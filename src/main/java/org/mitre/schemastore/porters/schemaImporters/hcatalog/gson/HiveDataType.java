/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.schemastore.porters.schemaImporters.hcatalog.gson;



/**
 *
 * @author mgreer
 */
public class HiveDataType {
    
        public enum DataType{
            TINYINT(), SMALLINT(), INT(), 
            BIGINT(), BOOLEAN(), 
            FLOAT(), DOUBLE(), STRING(), 
            BINARY(), TIMESTAMP(), DECIMAL(), 
            VARCHAR(), CHAR(), ARRAY(true), MAP(true), STRUCT(true),
            UNION(true), VOID(), TABLE(true), DATABASE(true);
        
            private boolean isComplex = false;
            private HiveDataType typeObj = null;
        
            private DataType() {
                typeObj = new HiveDataType(this);
            }
            private DataType(boolean complex) {
                isComplex = complex;
            }
            public boolean isComplex() {
                return isComplex;
            }
            public HiveDataType getHiveDataType() {
                return typeObj;
            }
        };
            
        protected transient DataType dataType;
        private transient String typeString;
        protected HiveDataType(DataType dataType) {
            this.dataType = dataType;
        }
        public DataType getDataType(){
            return dataType;
        }
        public String getTypeString() {
            return typeString;
        }
        public void setTypeString(String typeString) {
            this.typeString = typeString;
        }
       
    
    
    
}
