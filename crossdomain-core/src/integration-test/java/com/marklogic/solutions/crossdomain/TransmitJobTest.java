package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.jobs.transmit.TransmitJobProcessor;
import org.junit.Test;

public class TransmitJobTest {

    @Test
    public void runTransmitOneJob() {
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new TransmitJobProcessor());
        JobResult result = mgr.runJob();
        System.out.println("result startDate=" + result.getStart());
        System.out.println("result=" + result.getResultOutput());
        System.out.println("result endDate=" + result.getEnd());
    }
}