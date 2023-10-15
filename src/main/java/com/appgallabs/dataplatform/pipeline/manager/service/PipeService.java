package com.appgallabs.dataplatform.pipeline.manager.service;

import com.appgallabs.dataplatform.pipeline.manager.model.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PipeService {
    private static Logger logger = LoggerFactory.getLogger(PipeService.class);


    public void moveToDevelopment(Pipe pipe){

    }

    public void moveToStage(Pipe pipe){

    }

    public void moveToDeploy(Pipe pipe){

    }
}
