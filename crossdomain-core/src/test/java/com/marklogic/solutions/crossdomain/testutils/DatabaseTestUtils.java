package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.contentpump.ContentPump;
import com.marklogic.contentpump.utilities.OptionsFileUtil;
import com.marklogic.solutions.utils.ClasspathUtils;
import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DatabaseTestUtils extends Assert {

    String xccUrlString;

    public String getXccUrlString() {
        return xccUrlString;
    }

    public void setXccUrlString(String xccUrlString) {
        this.xccUrlString = xccUrlString;
    }

    private ContentSource contentSource;
    private Integer port;
    private String mlcpHome;

    public DatabaseTestUtils(String xccUrlString) {
        this.xccUrlString = xccUrlString;
        try {
            URI xccUrl = new URI(xccUrlString);
            contentSource = ContentSourceFactory.newContentSource(xccUrl);
            port = xccUrl.getPort();
        } catch (XccConfigException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String executeXquery(String xquery) {
        Session session = contentSource.newSession();
        Request request = session.newAdhocQuery(xquery);
        try {
            ResultSequence resultSequence = session.submitRequest(request);
            String response = "";
            while (resultSequence.hasNext()) {
                response += IOUtils.toString(resultSequence.next().getItem().asReader());
            }
            return response;
        } catch (RequestException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearDatabase() {
        executeXquery("xdmp:database-forests(xdmp:database()) ! xdmp:forest-clear(.)");
    }

    public void assertDocumentExistsInCollection(String uri, String collection) {
        assertTrue(String.format("URI = '%s' cannot be found in '%s' collection", uri, collection), "true".equals(executeXquery(String.format("xs:boolean(xdmp:document-get-collections('%s') = '%s')", uri, collection))));
    }

    public void assertDocumentExists(String uri) {
        assertTrue(String.format("URI = '%s' cannot be found", uri), "true".equals(executeXquery(String.format("xdmp:exists(fn:doc('%s'))", uri))));
    }

    public void assertDocumentDoesNotExist(String uri) {
        assertFalse(String.format("URI = '%s' should not be found", uri), "true".equals(executeXquery(String.format("xdmp:exists(fn:doc('%s'))", uri))));
    }

    public void loadTestDataFromClasspath(String classpathPath) {
        File loadDir = ClasspathUtils.getFileOrDirectoryFromClasspath(classpathPath);
        String loadDirStr = loadDir.getAbsolutePath();
        System.out.println("loadDirStr=" + loadDirStr);
        System.out.println("port=" + port.toString());
        try {
            String[] mlcpArgs = new String[]{"IMPORT", "-username", "admin", "-password", "admin",
                    "-input_file_path", loadDirStr, "-input_file_type", "archive",
                    "-host", "localhost", "-port", port.toString()};
            System.setProperty("CONTENTPUMP_HOME", mlcpHome);
            System.setProperty("hadoop.home.dir", mlcpHome);
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("xcc.txn.compatible", "true");
//            ContentPump.main(mlcpArgs);
            String[] expandedArgs = null;
            int rc = 1;
            try {
                expandedArgs = OptionsFileUtil.expandArguments(mlcpArgs);
                rc = ContentPump.runCommand(expandedArgs);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                System.err.println("Try 'mlcp help' for usage.");
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void runResourceScript(String resourceUrl, String... params) {
        try {
            String xquery = String.format(ClasspathUtils.getResourceContentsAsString("/test-data-scripts/" + resourceUrl), params);
            executeXquery(xquery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Use this function instead of runResourceScript with /generate-n-docs.xqy
     * when you want to ensure loop thru a process of adding each document.
     *
     * @param resourceUrl the XQY module to call
     * @param count	Number of documents to add
     */
    public void runDocInsertScript(String resourceUrl, int count) {
        try {
        	for(int i=1; i<=count; i++) {
        		String uri = i+"";
        		String xquery = String.format(ClasspathUtils.getResourceContentsAsString("/test-data-scripts/" + resourceUrl), uri);
        		executeXquery(xquery);
				Thread.sleep(1000);  // wait a second between each insert
        	}
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public String getMlcpHome() {
        return mlcpHome;
    }

    public void setMlcpHome(String mlcpHome) {
        this.mlcpHome = mlcpHome;
    }


    public void assertElementExistsInDocument(String uri, String xpath) {
        assertTrue(String.format("XPath '%s' not found in document with URI = '%s'", xpath, uri), !StringUtils.isBlank(executeXquery(String.format("fn:doc('%s')/%s", uri, xpath))));
    }
}
