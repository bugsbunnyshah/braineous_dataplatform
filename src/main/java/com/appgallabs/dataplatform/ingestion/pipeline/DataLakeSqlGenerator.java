package com.appgallabs.dataplatform.ingestion.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataLakeSqlGenerator {
    private static Logger logger = LoggerFactory.getLogger(DataLakeTableGenerator.class);

    public String generateInsertSql(String table, List<Map<String,Object>> rows){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO " + table + " VALUES {0}");
        String insertSqlTemplate = sqlBuilder.toString();

        StringBuilder batchBuilder = new StringBuilder();
        for(Map<String,Object> row:rows) {
            Collection<Object> values = row.values();
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("(");
            StringBuilder valueBuilder = new StringBuilder();
            for (Object value : values) {
                String insert = "'" + value + "',";
                valueBuilder.append(insert);
            }
            String valueBuilderStr = valueBuilder.toString();
            String rowValue = valueBuilderStr.substring(0, valueBuilderStr.length()-1);
            rowBuilder.append(rowValue);
            rowBuilder.append("),");
            batchBuilder.append(rowBuilder+"\n");
        }

        String batchBuilderStr = batchBuilder.toString();
        String insertValues = batchBuilderStr.substring(0, batchBuilderStr.length()-2);

        String insertSql = MessageFormat.format(insertSqlTemplate, insertValues);

        return insertSql;
    }
}
