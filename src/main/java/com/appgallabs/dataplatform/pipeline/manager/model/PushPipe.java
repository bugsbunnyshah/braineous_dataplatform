package com.appgallabs.dataplatform.pipeline.manager.model;

import java.util.List;

public class PushPipe extends Pipe{
    public PushPipe(String pipeId, String pipeName) {
        super(pipeId, pipeName);
    }

    public PushPipe(String pipeId, String pipeName, List<DataCleanerFunction> cleanerFunctions) {
        super(pipeId, pipeName, cleanerFunctions);
    }
}
