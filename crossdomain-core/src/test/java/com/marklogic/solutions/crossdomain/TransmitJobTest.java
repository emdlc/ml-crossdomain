package com.marklogic.solutions.crossdomain;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class TransmitJobTest {

    @Test
    public void runTransmitOneJob() {    	
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new TransmitJobItemsRetriever(), new TransmitJobProcessor());
        JobResult result = mgr.runJob();
        System.out.println("result startDate=" + result.getStart());
        System.out.println("result=" + result.getResultOutput());
        System.out.println("result endDate=" + result.getEnd());
    }
}