package prototype.ingestion;

import com.appgallabs.dataplatform.preprocess.SecurityToken;
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
    private RecordGenerator recordGenerator;

    @PostConstruct
    public void start(){
        this.registeredStores = new HashMap<>();
        this.registeredAgents = new HashMap<>();

        String pipeId = "staging_pipe";
        String registration = pipeId;

        SqlStorage sqlStorage = new SqlStorage();
        this.registeredStores.put(registration, sqlStorage);

        CoreDataIntegrationAgent agent = new CoreDataIntegrationAgent();
        this.registeredAgents.put(registration, agent);
    }

    public void receiveDataForStorage(SecurityToken securityToken,
                            String pipeId, String data){
        //Find the registered 'Storage' component for
        //this tenant/pipeId
        String registration = pipeId;
        Storage registeredStore = this.registeredStores.get(registration);

        //Parse the data into Records
        List<Record> records = this.recordGenerator.parsePayload(data);


        //Store the records into the Staging Area Store
        registeredStore.storeData(records);
    }

    public void runIntegrationAgent(SecurityToken securityToken,
                                    String pipeId){
        //Find the registered 'Agent' for this tenant/pipeId
        String registration = pipeId;
        DataIntegrationAgent agent = this.registeredAgents.get(registration);

        //Execute the agent and runners
        agent.executeIntegrationRunner();
    }
}
