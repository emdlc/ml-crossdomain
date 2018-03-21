package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

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

    public DatabaseTestUtils(String xccUrlString) {
        this.xccUrlString = xccUrlString;
        try {
            contentSource = ContentSourceFactory.newContentSource(new URI(xccUrlString));
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void clearDatabase() {
        executeXquery("xdmp:database-forests(xdmp:database()) ! xdmp:forest-clear(.)");
    }

    public void assertDocumentExists(String uri) {
        assertTrue("true".equals(executeXquery(String.format("xdmp:exists(fn:doc('%s'))", uri))));
    }

    public void assertDocumentDoesNotExist(String uri) {
        assertFalse("true".equals(executeXquery(String.format("xdmp:exists(fn:doc('%s'))", uri))));
    }
}
