package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.testutils.DatabaseAssert;
import com.marklogic.solutions.crossdomain.testutils.LandingZoneSetupUtils;
import com.marklogic.solutions.utils.ClasspathUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class ReceiveJobTest {

    DatabaseAssert dbAssert;

    @Before
    public void setupLandingZoneDirectoryAndClearReplicaDatabase() throws IOException {
        Properties testReceiveProps = ClasspathUtils.getPropertiesFileFromClasspath("/receiveJob.properties");
        LandingZoneSetupUtils.stageTestDataToLandingZone("/test-data", testReceiveProps.getProperty("landingzone.dir"));
        dbAssert = new DatabaseAssert(testReceiveProps.getProperty("ml.xcc.url"));
        dbAssert.clearDatabase();
    }

    @Test
    public void runReceiveOneJob() {
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        dbAssert.assertDocumentDoesNotExist("1234");
        JobResult result = mgr.runJob();
        dbAssert.assertDocumentExists("1234");
        System.out.println("result startDate=" + result.getStart());
        System.out.println("result=" + result.getResultOutput());
        System.out.println("result endDate=" + result.getEnd());
    }
}
