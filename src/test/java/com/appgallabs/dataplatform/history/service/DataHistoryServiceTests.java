package com.appgallabs.dataplatform.history.service;

import com.appgallabs.dataplatform.infrastructure.MongoDBJsonStore;
import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;
import test.components.BaseTest;
import com.appgallabs.dataplatform.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DataHistoryServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataHistoryServiceTests.class);

    @Inject
    private DataHistoryService service;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void getDataSnapShot() throws Exception {
        //ingestion0
        OffsetDateTime ingestion0Time = OffsetDateTime.now(ZoneOffset.UTC);
        JsonArray ingestion0 = this.mockIngestion(ingestion0Time,2);
        this.performIngestion(ingestion0);

        //ingestion1
        OffsetDateTime ingestion1Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion1Time = ingestion1Time.plus(5, ChronoUnit.MINUTES);
        JsonArray ingestion1 = this.mockIngestion(ingestion1Time,3);
        this.performIngestion(ingestion1);

        //ingestion2
        OffsetDateTime ingestion2Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion2Time = ingestion2Time.plus(10, ChronoUnit.MINUTES);
        JsonArray ingestion2 = this.mockIngestion(ingestion2Time,2);
        this.performIngestion(ingestion2);

        //ingestion3
        OffsetDateTime ingestion3Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion3Time = ingestion3Time.plus(15, ChronoUnit.MINUTES);
        JsonArray ingestion3 = this.mockIngestion(ingestion3Time,3);
        this.performIngestion(ingestion3);

        //Generate State
        JsonArray snapShot = this.service.getDataSnapShot(ingestion1Time,ingestion2Time);
        JsonUtil.print(snapShot);
        assertEquals(7,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion1Time,ingestion1Time);
        JsonUtil.print(snapShot);
        assertEquals(5,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion0Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(10,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion1Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(10,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion2Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(10,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion2Time,ingestion2Time);
        JsonUtil.print(snapShot);
        assertEquals(7,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion3Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(10,snapShot.size());
    }

    @Test
    public void getDataSnapShotRepeatingData() throws Exception {
        Map<Integer,String> oids = new HashMap<>();
        oids.put(0,UUID.randomUUID().toString());
        oids.put(1,UUID.randomUUID().toString());
        oids.put(2,UUID.randomUUID().toString());
        oids.put(3,UUID.randomUUID().toString());
        oids.put(4,UUID.randomUUID().toString());

        //ingestion0
        OffsetDateTime ingestion0Time = OffsetDateTime.now(ZoneOffset.UTC);
        JsonArray ingestion0 = this.mockIngestion(oids, ingestion0Time,1);//1
        this.performIngestion(ingestion0);

        //ingestion1
        OffsetDateTime ingestion1Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion1Time = ingestion1Time.plus(5, ChronoUnit.MINUTES);
        JsonArray ingestion1 = this.mockIngestion(oids,ingestion1Time,3); //2
        this.performIngestion(ingestion1);

        //ingestion2
        OffsetDateTime ingestion2Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion2Time = ingestion2Time.plus(10, ChronoUnit.MINUTES);
        JsonArray ingestion2 = this.mockIngestion(oids,ingestion2Time,4); //1
        this.performIngestion(ingestion2);

        //ingestion3
        OffsetDateTime ingestion3Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion3Time = ingestion3Time.plus(15, ChronoUnit.MINUTES);
        JsonArray ingestion3 = this.mockIngestion(oids,ingestion3Time,5); //1
        this.performIngestion(ingestion3);

        //Generate State
        JsonArray snapShot = this.service.getDataSnapShot(ingestion1Time,ingestion2Time);
        JsonUtil.print(snapShot);
        assertEquals(4,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion1Time,ingestion1Time);
        JsonUtil.print(snapShot);
        assertEquals(3,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion0Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(5,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion1Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(5,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion2Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(5,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion2Time,ingestion2Time);
        JsonUtil.print(snapShot);
        assertEquals(4,snapShot.size());

        snapShot = this.service.getDataSnapShot(ingestion3Time,ingestion3Time);
        JsonUtil.print(snapShot);
        assertEquals(5,snapShot.size());
    }

    private JsonArray mockIngestion(OffsetDateTime ingestionTime,int size) throws NoSuchAlgorithmException {
        JsonArray ingestion = new JsonArray();
        for(int i=0; i<size; i++){
            JsonObject data = new JsonObject();
            ingestion.add(data);
            Tenant tenant = this.securityTokenContainer.getTenant();
            String dataLakeId = UUID.randomUUID().toString();;
            String chainId = "/" + tenant.getPrincipal() + "/" + dataLakeId;
            data.addProperty("oid",UUID.randomUUID().toString());
            data.addProperty("dataLakeId",dataLakeId);
            data.addProperty("tenant",tenant.getPrincipal());
            data.addProperty("chainId",chainId);
            String objectHash = JsonUtil.getJsonHash(data);
            data.addProperty("timestamp",ingestionTime.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        return ingestion;
    }

    private JsonArray mockIngestion(Map<Integer,String> oids,OffsetDateTime ingestionTime, int size) throws NoSuchAlgorithmException {
        JsonArray ingestion = new JsonArray();
        for(int i=0; i<size; i++){
            JsonObject data = new JsonObject();
            ingestion.add(data);
            Tenant tenant = this.securityTokenContainer.getTenant();
            String dataLakeId = UUID.randomUUID().toString();;
            String chainId = "/" + tenant.getPrincipal() + "/" + dataLakeId;
            data.addProperty("oid",oids.get(i));
            data.addProperty("dataLakeId",dataLakeId);
            data.addProperty("tenant",tenant.getPrincipal());
            data.addProperty("chainId",chainId);
            String objectHash = JsonUtil.getJsonHash(data);
            data.addProperty("timestamp",ingestionTime.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        return ingestion;
    }

    private void performIngestion(JsonArray ingestion){
        Tenant tenant = this.securityTokenContainer.getTenant();

        Iterator<JsonElement> iterator = ingestion.iterator();
        while(iterator.hasNext()){
            this.mongoDBJsonStore.storeHistoryObject(tenant,iterator.next().getAsJsonObject());
        }
    }
}
