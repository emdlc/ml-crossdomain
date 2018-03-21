package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.testutils.DatabaseTestUtils;
import com.marklogic.solutions.crossdomain.testutils.LandingZoneTestUtils;
import com.marklogic.solutions.utils.ClasspathUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class ReceiveJobTest {

    DatabaseTestUtils dbUtils;
    LandingZoneTestUtils lzUtils;

    @Before
    public void setupLandingZoneDirectoryAndClearReplicaDatabase() throws IOException {
        Properties testReceiveProps = ClasspathUtils.getPropertiesFileFromClasspath("/receiveJob.properties");
        lzUtils = new LandingZoneTestUtils(testReceiveProps.getProperty("landingzone.dir"));
        lzUtils.clearLandingZone();
        dbUtils = new DatabaseTestUtils(testReceiveProps.getProperty("ml.xcc.url"));
        dbUtils.clearDatabase();
    }

    @Test
    public void runReceiveOneJob() throws IOException {
        lzUtils.stageTestDataToLandingZone("/test-data/receive/working");
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        dbUtils.assertDocumentDoesNotExist("1235");
        JobResult result = mgr.runJob();
        dbUtils.assertDocumentExists("1235");
        System.out.println("result startDate=" + result.getStart());
        System.out.println("result=" + result.getResultOutput());
        System.out.println("result endDate=" + result.getEnd());
    }
}
