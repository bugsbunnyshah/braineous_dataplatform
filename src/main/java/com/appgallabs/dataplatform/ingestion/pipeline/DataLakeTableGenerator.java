package com.appgallabs.dataplatform.ingestion.pipeline;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.TableDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DataLakeTableGenerator {
    private static Logger logger = LoggerFactory.getLogger(DataLakeTableGenerator.class);

    public TableDescriptor createFileSystemTable(Map<String,Object> row,
                                                 String filePath,
                                                 String format){
        Schema.Builder schemaBuilder = Schema.newBuilder();
        Set<String> columnNames = row.keySet();

        for(String columnName: columnNames){
            schemaBuilder.column(columnName, DataTypes.STRING());
        }

        Schema schema = schemaBuilder.build();

        TableDescriptor tableDescriptor = TableDescriptor.forConnector("filesystem")
                .option("path", filePath)
                .option("format", format)
                .schema(schema)
                .build();

        return tableDescriptor;
    }
}
