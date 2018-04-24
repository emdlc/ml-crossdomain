package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.testutils.DatabaseTestUtils;
import com.marklogic.solutions.crossdomain.testutils.LandingZoneTestUtils;
import com.marklogic.solutions.utils.ClasspathUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;


public class ReceiveJobBadFileTest {

	private static final Logger logger = Logger.getLogger(ReceiveJobBadFileTest.class);
	
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
    public void runReceiveOneJobBadFile() throws IOException {
        Properties testReceiveProps = ClasspathUtils.getPropertiesFileFromClasspath("/receiveJob.properties");
		lzUtils.stageTestDataToLandingZone("/test-data/receive/badFile");
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        JobResult result = mgr.runJob();
		dbUtils.assertDocumentDoesNotExist("1241");
		lzUtils.assertFilePathExists(testReceiveProps.getProperty("landingzone.error.dir") + "/cds-00003.jar");
        logger.info("result startDate=" + result.getStart());
        logger.info("result=" + result.getResultOutput());
        logger.info("result endDate=" + result.getEnd());
    }
}
