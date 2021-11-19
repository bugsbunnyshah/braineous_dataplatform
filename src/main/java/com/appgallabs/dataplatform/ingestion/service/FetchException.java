package com.appgallabs.dataplatform.ingestion.service;

public class FetchException extends Exception
{
    public FetchException(String message)
    {
        super(message);
    }

    public FetchException(Exception source)
    {
        super(source);
    }
}
