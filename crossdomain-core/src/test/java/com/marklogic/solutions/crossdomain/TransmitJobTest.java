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
        dbUtils = new DatabaseTestUtils(testTransmitProps.getProperty("ml.xcc.url"));
        dbUtils.setMlcpHome(testTransmitProps.getProperty("mlcp.home"));
        lzUtils.clearLandingZone();
        dbUtils.clearDatabase();
    }

    @Test
    public void runTransmitOneJob() {
        dbUtils.loadTestDataFromClasspath("/test-data/transmit/simpleTest");
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new TransmitJobProcessor());
        JobResult result = mgr.runJob();
        System.out.println(result.getResultOutput());

        // 1 JAR file expected. It contains the content documents wrapped in cds:Envelope and the status XML doc
        lzUtils.assertJarFilesInLandingZone(1);	
        lzUtils.assertContentFileForUriInJar("12360");
        
        // TODO: implement
        //lzUtils.assertJarIsSigned();
        
        //TODO: Implement the following assertions
//      lzUtils.assertContentAndMetadataFileForUriInJar();
    }

    @Test
    public void runTransmitOfFiftyDocuments() {
        dbUtils.runResourceScript("transmit/generate-n-docs.xqy", "50");
        TransmitJobProcessor jobProcessor = new TransmitJobProcessor();
        SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(jobProcessor);
        JobResult result = mgr.runJob();
        System.out.println(result.getResultOutput());

        // 2 files expected. The envelope Jar and the status Jar
        lzUtils.assertJarFilesInLandingZone(10);
        lzUtils.assertFileComeInOrder();

    }
}
