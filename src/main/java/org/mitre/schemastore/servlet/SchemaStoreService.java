/**
 * SchemaStoreService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.mitre.schemastore.servlet;

public interface SchemaStoreService extends javax.xml.rpc.Service {
    public String getSchemaStoreAddress();

    public SchemaStoreObject getSchemaStore() throws javax.xml.rpc.ServiceException;

    public SchemaStoreObject getSchemaStore(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
