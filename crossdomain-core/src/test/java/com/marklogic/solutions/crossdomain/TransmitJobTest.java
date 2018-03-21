package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.jobs.transmit.TransmitJobProcessor;
import com.marklogic.solutions.crossdomain.testutils.DatabaseTestUtils;
import com.marklogic.solutions.crossdomain.testutils.LandingZoneTestUtils;
import com.marklogic.solutions.utils.ClasspathUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class TransmitJobTest {

    DatabaseTestUtils dbUtils;
    LandingZoneTestUtils lzUtils;

    @Before
    public void setup() throws IOException {
        Properties testTransmitProps = ClasspathUtils.getPropertiesFileFromClasspath("/transmitJob.properties");
        lzUtils = new LandingZoneTestUtils(testTransmitProps.getProperty("landingzone.dir"));
        lzUtils.stageTestDataToLandingZone("/test-data");
        dbUtils = new DatabaseTestUtils(testTransmitProps.getProperty("ml.xcc.url"));
        lzUtils.clearLandingZone();
    }

    @Test
    public void runReceiveOneJob() {
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new TransmitJobProcessor());
        // TODO:
    }
}
