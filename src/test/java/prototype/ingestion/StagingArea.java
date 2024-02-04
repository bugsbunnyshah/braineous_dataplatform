package prototype.ingestion;

import com.appgallabs.dataplatform.infrastructure.Tenant;
import com.appgallabs.dataplatform.preprocess.SecurityToken;

import com.appgallabs.dataplatform.preprocess.SecurityTokenContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class StagingArea {
    private static Logger logger = LoggerFactory.getLogger(StagingArea.class);

    private Map<String, Storage> registeredStores;
    private Map<String, DataIntegrationAgent> registeredAgents;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private RecordGenerator recordGenerator;

    @PostConstruct
    public void start(){
        //TODO: integrate with pipeline_registry (CR2)
        this.registeredStores = new HashMap<>();
        this.registeredAgents = new HashMap<>();

        String pipeId = "staging_pipe";
        String registration = pipeId;

        SqlStorage sqlStorage = new SqlStorage();
        this.registeredStores.put(registration, sqlStorage);

        CoreDataIntegrationAgent agent = new CoreDataIntegrationAgent();
        this.registeredAgents.put(registration, agent);
    }

    public void receiveDataForStorage(String pipeId, String data){
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        Tenant tenant = this.securityTokenContainer.getTenant();

        //Find the registered 'Storage' component for
        //this tenant/pipeId
        String registration = pipeId;
        Storage registeredStore = this.registeredStores.get(registration);

        //Parse the data into Records
        List<Record> records = this.recordGenerator.parsePayload(data);


        //Store the records into the Staging Area Store
        registeredStore.storeData(records);
    }

    public void runIntegrationAgent(String pipeId){
        Tenant tenant = this.securityTokenContainer.getTenant();

        //Find the registered 'Agent' for this tenant/pipeId
        String registration = pipeId;
        DataIntegrationAgent agent = this.registeredAgents.get(registration);

        //Execute the agent and runners
        agent.executeIntegrationRunner(tenant, pipeId);
    }
}
