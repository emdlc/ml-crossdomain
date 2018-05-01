package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.testutils.DatabaseTestUtils;
import com.marklogic.solutions.crossdomain.testutils.LandingZoneTestUtils;
import com.marklogic.solutions.utils.ClasspathUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;


public class ReceiveJobTest {

    private static final Logger logger = Logger.getLogger(ReceiveJobTest.class);

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
        Properties testReceiveProps = ClasspathUtils.getPropertiesFileFromClasspath("/receiveJob.properties");
        lzUtils.stageTestDataToLandingZone("/test-data/receive/working");
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        dbUtils.assertDocumentDoesNotExist("1235");
        JobResult result = mgr.runJob();
        dbUtils.assertDocumentExists("1235");
        lzUtils.assertFilePathExists(testReceiveProps.getProperty("landingzone.archive.dir") + "/cds-00001.jar");
        logger.info("result startDate=" + result.getStart());
        logger.info("result=" + result.getResultOutput());
        logger.info("result endDate=" + result.getEnd());
    }

    @Test
    public void runReceiveFiftyDocs() throws IOException {
        lzUtils.stageTestDataToLandingZone("/test-data/receive/multiple-jars");
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
        JobResult result = mgr.runJob();
        Assert.assertTrue("Status Document not found", !StringUtils.isBlank(dbUtils.executeXquery("declare namespace cds=\"http://marklogic.com/mlcs/cds\"; /cds:StatusEnvelope")));
        Assert.assertTrue("Status Document Total Count not 51", dbUtils.executeXquery("declare namespace cds=\"http://marklogic.com/mlcs/cds\"; /cds:StatusEnvelope/cds:TotalCount/text()").equals("51"));
        Assert.assertTrue("Status Document 'person' collection count not 50", dbUtils.executeXquery("declare namespace cds=\"http://marklogic.com/mlcs/cds\"; /cds:StatusEnvelope/cds:DomainCollection[. = 'person']/@count").equals("50"));
        for (int i = 1; i <= 50; i++) {
            String uri = "/data/" + i + ".xml";
            dbUtils.assertDocumentExistsInCollection(uri, "datum");
            dbUtils.assertDocumentExistsInCollection(uri, "person");
            dbUtils.assertDocumentExistsInCollection(uri, "entity");
            dbUtils.assertElementExistsInDocument(uri, String.format("/person/id[. = '%s']", i));


        }

    }
}
