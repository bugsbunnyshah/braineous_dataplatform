package com.appgallabs.dataplatform.infrastructure.kafka;

import com.appgallabs.dataplatform.ingestion.algorithm.SchemalessMapper;
import com.appgallabs.dataplatform.ingestion.pipeline.PipelineService;
import com.appgallabs.dataplatform.ingestion.pipeline.SystemStore;
import com.appgallabs.dataplatform.pipeline.Registry;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.targetSystem.framework.StoreOrchestrator;
import com.appgallabs.dataplatform.util.Debug;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * The class implements the processMessage() method. Typically, this class is used
 * to supply callback behavior for this project's producers and consumers.
 */
public class EventHandler implements KafkaMessageHandler {
    static Logger log = LoggerFactory.getLogger(EventHandler.class.getName());

    private PipelineService pipelineService;
    private StoreOrchestrator storeOrchestrator;

    private SystemStore systemStore;

    private SchemalessMapper schemalessMapper;

    ExecutorService threadpool = Executors.newCachedThreadPool();

    public EventHandler(PipelineService pipelineService,
                        SystemStore systemStore,
                        SchemalessMapper schemalessMapper,
                        StoreOrchestrator storeOrchestrator){
        this.pipelineService = pipelineService;
        this.systemStore = systemStore;
        this.schemalessMapper = schemalessMapper;
        this.storeOrchestrator = storeOrchestrator;
    }

    public void shutdown(){
        this.threadpool.shutdown();
    }

    @Override
    public void processMessage(String topicName, ConsumerRecord<String, String> message) throws Exception {
        String position = "PARTITION: " + message.partition() + "-" + "OFFSET: " + message.offset();
        long offset = message.offset();
        String messageValue = message.value();
        String pipeId = topicName;

        /*Debug.out("************************");
        Debug.out("position: "+position);
        Debug.out("message: "+messageValue);
        Debug.out("************************");*/

        JsonObject json = JsonParser.parseString(messageValue).getAsJsonObject();

        String payload = json.get("message").getAsString();
        JsonElement payloadElem = JsonParser.parseString(payload);

        String jsonPayloadString = payloadElem.toString();

        //SecurityToken
        String securityTokenString = json.get("securityToken").getAsString();
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenString);

        //Entity
        String entity = json.get("entity").getAsString();


        JsonObject datalakeDriverConfiguration = Registry.getInstance().getDatalakeConfiguration();
        this.executeIngestion(securityToken, datalakeDriverConfiguration.toString(),
                pipeId, offset,entity,jsonPayloadString);

        /*this.executeTargetSystemDelivery(securityToken,
                this.systemStore,
                this.schemalessMapper,
                pipeId,
                jsonPayloadString);*/
    }

    private void executeIngestion(SecurityToken securityToken,
                                  String datalakeDriverConfiguration,
                                  String pipeId, long offset, String entity,String jsonPayloadString){
        this.threadpool.execute(() -> {
            this.pipelineService.ingest(securityToken, datalakeDriverConfiguration,
                    pipeId, offset,entity,jsonPayloadString);
        });
    }

    private void executeTargetSystemDelivery(SecurityToken securityToken,
                                             SystemStore systemStore,
                                             SchemalessMapper schemalessMapper,
                                             String pipeId, String jsonPayloadString){
        this.threadpool.execute(() -> {
            this.storeOrchestrator.receiveData(securityToken,
                    systemStore,
                    this.pipelineService.getFlinkHost(),
                    this.pipelineService.getFlinkPort(),
                    schemalessMapper,
                    pipeId,
                    jsonPayloadString);
        });
    }
}
