package com.appgallabs.dataplatform.pipeline.manager.model;

import java.util.List;

public class PollPipe extends Pipe{
    public PollPipe(String pipeId, String pipeName) {
        super(pipeId, pipeName);
    }

    public PollPipe(String pipeId, String pipeName, List<DataCleanerFunction> cleanerFunctions) {
        super(pipeId, pipeName, cleanerFunctions);
    }
}
