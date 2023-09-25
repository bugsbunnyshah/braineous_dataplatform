package com.appgallabs.dataplatform.query.graphql;

import com.appgallabs.dataplatform.datalake.DataLakeDriver;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import com.google.gson.JsonArray;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class QueryService {

    private List<DataLakeRecord> records = new ArrayList();

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private Instance<DataLakeDriver> dataLakeDriverInstance;
    private String dataLakeDriverName;
    private DataLakeDriver dataLakeDriver;

    @PostConstruct
    public void start(){
        Config config = ConfigProvider.getConfig();
        this.dataLakeDriverName = config.getValue("datalake_driver_name", String.class);
        this.dataLakeDriver = dataLakeDriverInstance.select(NamedLiteral.of(dataLakeDriverName)).get();
    }


    public QueryService() {
        DataLakeRecord r1 = new DataLakeRecord();
        r1.setDataLakeId(UUID.randomUUID().toString());

        records.add(r1);
    }

    public List<DataLakeRecord> allRecords() {
        return this.records;
    }

    public List<Document> getDocumentByLakeId(String datalakeId){
        //Tenant tenant = this.securityTokenContainer.getTenant();
        //JsonArray document = this.dataLakeDriver.readIngestion(tenant,datalakeId);

        Document document = new Document();
        document.setData("HELLO_WORLD");
        document.setDataLakeId(datalakeId);

        List<Document> documents = new ArrayList<>();
        documents.add(document);

        return documents;
    }
}
