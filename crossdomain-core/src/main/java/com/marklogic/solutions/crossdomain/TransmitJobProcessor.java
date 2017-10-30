package com.marklogic.solutions.crossdomain;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

public class TransmitJobProcessor extends JobProcessor<String> {

	private static final int BATCHSIZE = 5;
	private static final String landingZoneDir = "/Users/jkendric/dev/tmp/cds-landingzone/";
	private static final String xccURL = "xcc://admin:admin@localhost:9102";

	@Override
	public JobResult executeJob() {

		Date jobStartDate = new Date();

		ZipOutputStream out = null;
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		int batchId = 0;
		
		if(queue.size() > 0) {
			String statusDocument = null;
			try {
				statusDocument = getStatusDocument();
				String jarfilename = landingZoneDir + timeStamp + "-DB-SYNC-status.jar";
				String jarContentFileName = timeStamp + "-DB-data-status.xml";
				writeStatusDocumentToZip(statusDocument, jarfilename, jarContentFileName);
			} catch (XccConfigException | RequestException | URISyntaxException | IOException e) {
				e.printStackTrace();
			}
		}

		while (queue.size() > 0) {
			// get BATCHSIZE uris from queue
			ArrayList<String> uris = new ArrayList<String>();
			for (int i = 0; (i < BATCHSIZE && queue.size() > 0); i++) {
				String uri = null;
				try {
					uri = queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				uris.add(uri);
			}

			batchId++;
			String jarfilename = null;
			
			try {
				if (uris.size() > 0) {
					jarfilename = landingZoneDir + timeStamp + "-DB-SYNC-" + batchId + ".jar";
					out = new ZipOutputStream(new FileOutputStream(jarfilename));
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			int uriCounter = 0;
			for (String uri : uris) {
				String docContents = null;
				uriCounter++;
				try {
					docContents = getDocumentFromDatabase(uri);
					String datafilename = timeStamp + "-DB-data-" + batchId + "-" + uriCounter + ".xml";
					out.putNextEntry(new ZipEntry(datafilename));
					byte[] inputBytes = docContents.getBytes();
					out.write(inputBytes);
				} catch (XccConfigException | RequestException | URISyntaxException | IOException e) {
					e.printStackTrace();
				}
			}

			// close output write
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		JobResult jobResult = new JobResult();
		jobResult.setStart(jobStartDate);
		jobResult.setEnd(new Date());
		jobResult.setResultOutput("Completed TransmitJob");

		return jobResult;
	}

	private void writeStatusDocumentToZip(String statusDocument, String zipFileName, String zipContentFileName) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		
		out.putNextEntry(new ZipEntry(zipContentFileName));
		byte[] inputBytes = statusDocument.getBytes();
		out.write(inputBytes);
		
		// close output write
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getStatusDocument() throws XccConfigException, URISyntaxException, RequestException {
		ContentSource cs;
		cs = ContentSourceFactory.newContentSource(new URI(xccURL));
		cs.setAuthenticationPreemptive(true);
		Session session = cs.newSession();

		InputStream stream = getClasspathContentAsStream("/build-status-document.xqy");

		String mlQuery = null;
		try {
			mlQuery = IOUtils.toString(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
						
		Request request = session.newAdhocQuery(mlQuery);
		ResultSequence rs = session.submitRequest(request);
		return rs.asString();
	}

	private String getDocumentFromDatabase(String uri)
			throws XccConfigException, URISyntaxException, RequestException {
		ContentSource cs;
		cs = ContentSourceFactory.newContentSource(new URI(xccURL));
		cs.setAuthenticationPreemptive(true);
		Session session = cs.newSession();
		
		InputStream stream = getClasspathContentAsStream("/wrap-document.xqy");

		String mlQuery = null;
		try {
			mlQuery = IOUtils.toString(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
						
		Request request = session.newAdhocQuery(mlQuery);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("URI", uri);
		for (Entry<String, String> entry : params.entrySet()) {
			String mapKey = entry.getKey();
			String mapValue = entry.getValue();
			XdmValue value = ValueFactory.newXSString(mapValue);
			XName xname = new XName("", mapKey);
			XdmVariable myVar = ValueFactory.newVariable(xname, value);
			request.setVariable(myVar);
		}
		ResultSequence rs = session.submitRequest(request);
		return rs.asString();
	}
	
	public static InputStream getClasspathContentAsStream(String path) {
		return TransmitJobProcessor.class.getResourceAsStream(path);
	}
}
