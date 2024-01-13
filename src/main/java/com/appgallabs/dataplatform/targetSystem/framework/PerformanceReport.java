package com.appgallabs.dataplatform.targetSystem.framework;

public class PerformanceReport {
    boolean started = false;
    int counter = 0;

    long start;
    long end;

    @Override
    public String toString() {
        long duration = end - start;
        return ""+duration;
    }
}
