package com.marklogic.solutions.crossdomain;

import java.io.File;

import org.junit.Test;

public class ReceiveJobTest {

    @Test
    public void runReceiveOneJob() {
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        JobResult result = mgr.runJob();
        System.out.println("result startDate=" + result.getStart());
        System.out.println("result=" + result.getResultOutput());
        System.out.println("result endDate=" + result.getEnd());
    }
}
