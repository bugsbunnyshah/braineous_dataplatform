package com.appgallabs.dataplatform.query.graphql;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class QueryService {

    private List<DataLakeRecord> records = new ArrayList();


    public QueryService() {
        DataLakeRecord r1 = new DataLakeRecord();
        r1.setDataLakeId(UUID.randomUUID().toString());

        records.add(r1);
    }

    public List<DataLakeRecord> allRecords() {
        return this.records;
    }
}
