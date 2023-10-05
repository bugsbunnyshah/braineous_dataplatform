package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.DataCleanerFunction;
import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataCleansingService {

    private static Logger logger = LoggerFactory.getLogger(DataCleansingService.class);


    public void addCleanerFunction(Pipe pipe, DataCleanerFunction dataCleanerFunction){

    }

    public void removeCleanerFunction(Pipe pipe, DataCleanerFunction dataCleanerFunction){

    }

    public void removeAllCleanerFunctions(Pipe pipe){

    }
}
