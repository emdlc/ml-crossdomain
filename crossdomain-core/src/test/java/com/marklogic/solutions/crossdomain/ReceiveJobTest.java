package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.utils.ClasspathUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ReceiveJobTest {

    @Before
    public void setupReceiveDirectory() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Properties testReceiveProps = ClasspathUtils.getPropertiesFileFromClasspath("/receiveJob.properties");

        File testDataDir = ClasspathUtils.getFileOrDirectoryFromClasspath("/test-data");
        File lzDir = new File(testReceiveProps.getProperty("landingzone.dir"));
        lzDir.mkdirs();
        FileUtils.copyDirectory(testDataDir, lzDir);
    }

    @Test
    public void runReceiveOneJob() {
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        JobResult result = mgr.runJob();
        System.out.println("result startDate=" + result.getStart());
        System.out.println("result=" + result.getResultOutput());
        System.out.println("result endDate=" + result.getEnd());
    }
}
