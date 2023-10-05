package com.appgallabs.dataplatform.pipeline.manager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pipe implements Serializable {
    private String pipeId;
    private String pipeName;
    private List<DataCleanerFunction> cleanerFunctions;

    public Pipe(String pipeId, String pipeName) {
        this.pipeId = pipeId;
        this.pipeName = pipeName;
        this.cleanerFunctions = new ArrayList<>();
    }

    public Pipe(String pipeId, String pipeName, List<DataCleanerFunction> cleanerFunctions) {
        if(cleanerFunctions == null || cleanerFunctions.isEmpty()){
            cleanerFunctions = new ArrayList<>();
        }

        this.pipeId = pipeId;
        this.pipeName = pipeName;
        this.cleanerFunctions = cleanerFunctions;
    }

    public String getPipeId() {
        return pipeId;
    }

    public void setPipeId(String pipeId) {
        this.pipeId = pipeId;
    }

    public String getPipeName() {
        return pipeName;
    }

    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }

    public List<DataCleanerFunction> getCleanerFunctions() {
        return cleanerFunctions;
    }

    public void setCleanerFunctions(List<DataCleanerFunction> cleanerFunctions) {
        this.cleanerFunctions = cleanerFunctions;
    }

    public void addCleanerFunction(DataCleanerFunction cleanerFunction){
        if(cleanerFunction != null){
            this.cleanerFunctions.add(cleanerFunction);
        }
    }

    public void removeCleanerFunction(DataCleanerFunction cleanerFunction){
        if(cleanerFunction != null){
            this.cleanerFunctions.remove(cleanerFunction);
        }
    }

    public void clearClearFunctions(){
        this.cleanerFunctions.clear();
    }
}
