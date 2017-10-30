package com.marklogic.solutions.crossdomain;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.io.IOUtils;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

public class TransmitJobItemsRetriever implements JobItemsRetriever<String> {

	private static final String xccURL = "xcc://user:password@localhost:9102";
	
	private ArrayBlockingQueue<String> queue;
	
	public TransmitJobItemsRetriever() {
		this.queue = new ArrayBlockingQueue<String>(10000);
	}

	@Override
	public ArrayBlockingQueue<String> retrieve(Map<String, Object> params) {

		// get URIs and add them to the queue
		String mlResponse = getURIsToTransmit();
		
		String[] uris = mlResponse.split("\n");
		for (int i = 0; i < uris.length; i++) {
			try {
				queue.put(uris[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.queue;
	}

	private String getURIsToTransmit() {
		Map<String, String> params = new HashMap<String, String>();
		// params.put("statusTimeStamp", statusTimeStamp);
		String MLoutput = null;
		try {
			MLoutput = callMLModule(null, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return MLoutput;
	}

	private String callMLModule(String moduleURI, Map<String, String> args)
			throws Exception {
		String mlResponse = null;
		ContentSource cs;

		String queryString = getAllUrisQuery();

		cs = ContentSourceFactory.newContentSource(new URI(xccURL));
		cs.setAuthenticationPreemptive(true);
		Session session = cs.newSession();

		Request request = session.newAdhocQuery(queryString);

		for (Entry<String, String> entry : args.entrySet()) {
			String mapKey = entry.getKey();
			String mapValue = entry.getValue();
			XdmValue value = ValueFactory.newXSString(mapValue);
			XName xname = new XName("", mapKey);
			XdmVariable myVar = ValueFactory.newVariable(xname, value);
			request.setVariable(myVar);
		}

		ResultSequence rs = session.submitRequest(request);
		mlResponse = rs.asString();

		return mlResponse;
	}

	@SuppressWarnings("deprecation")
	private String getAllUrisQuery() {

		InputStream stream = getClasspathContentAsStream("/get-uris-for-transmit-job.xqy");

		String ctsUrisQuery = "";
		try {
			ctsUrisQuery = IOUtils.toString(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ctsUrisQuery;
		
	}
	
	public static InputStream getClasspathContentAsStream(String path) {
		return TransmitJobItemsRetriever.class.getResourceAsStream(path);
	}
	
}
