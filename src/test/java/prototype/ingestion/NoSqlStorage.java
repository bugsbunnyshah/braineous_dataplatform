package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;

import java.util.List;

public class NoSqlStorage implements Storage{
    @Override
    public void storeData(Tenant tenant, String pipeId, List<Record> dataset) {

    }
}
