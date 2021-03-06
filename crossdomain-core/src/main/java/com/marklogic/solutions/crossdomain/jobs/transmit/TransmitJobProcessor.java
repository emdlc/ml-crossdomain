package com.marklogic.solutions.crossdomain.jobs.transmit;

import com.marklogic.solutions.crossdomain.JobProcessor;
import com.marklogic.solutions.crossdomain.JobResult;
import com.marklogic.solutions.utils.ClasspathUtils;
import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TransmitJobProcessor extends JobProcessor<String> {
	
	private int BATCHSIZE;
	private String landingZoneDir;
	private String xccURL;
	private boolean signJar;
	private String keystoreFilePath;
	private String keystoreAlias;
	private String keystorePassword;
	
	@Override
	public JobResult executeJob() {

		Date jobStartDate = new Date();

		ZipOutputStream out = null;
		String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		int batchId = 0;		

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
			String jarFileName = null;
			
			try {
				if (uris.size() > 0) {
					jarFileName = landingZoneDir + timeStamp + "-DB-SYNC-" + String.format("%04d", batchId) + ".jar";
					out = new ZipOutputStream(new FileOutputStream(jarFileName));
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
					out.putNextEntry(new ZipEntry(uri));
					byte[] inputBytes = docContents.getBytes();
					out.write(inputBytes);
					
					if(uriCounter == uris.size()) {
						// just added the last content document to the zip; now add the status document
						String statusDocument = getStatusDocument();
						String jarContentFileName = timeStamp + "-DB-data-status.xml";
						out.putNextEntry(new ZipEntry(jarContentFileName));
						inputBytes = statusDocument.getBytes();
						out.write(inputBytes);						
					}
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
			
			// sign the jar - 
			// sign the jar (this can be disabled by setting the signJar property to 'false')
			if(signJar) {
				try {
					signJar(jarFileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		JobResult jobResult = new JobResult();
		jobResult.setStart(jobStartDate);
		jobResult.setEnd(new Date());
		jobResult.setResultOutput("Completed TransmitJob");

		return jobResult;
	}

	private void signJar(String jarFileName) throws IOException, InterruptedException {
		File keystoreFile = ClasspathUtils.getFileOrDirectoryFromClasspath(this.keystoreFilePath);
		String keystoreFileDir = keystoreFile.getAbsolutePath();
		String [] commands = {"jarsigner", 
								"-verbose",
								"-keystore",  keystoreFileDir, 
								"-storepass", keystorePassword,
								//"-signedjar",  "Signed-"+jarFileName,  // this flag allows signed JAR to have another filename
								jarFileName, keystoreAlias};

		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		Process p = processBuilder.redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
		p.waitFor();
		int exitCode = p.exitValue();
		//System.out.println("jarsigner exit code="+exitCode);
	}

	public TransmitJobProcessor() {
		super();
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {			
			String filename = "transmitJob.properties";
			input = TransmitJobProcessor.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null) {
    	            System.out.println("unable to find file:" + filename);
    		    return;
    		}
			
			// load a properties file
			prop.load(input);
			
			this.BATCHSIZE=Integer.parseInt(prop.getProperty("zip.maxFileCount"));
			this.landingZoneDir=prop.getProperty("landingzone.dir");
			this.xccURL=prop.getProperty("ml.xcc.url");
			this.signJar=Boolean.parseBoolean(prop.getProperty("signJar"));
			
			this.keystoreFilePath=prop.getProperty("keystore.filepath");
			this.keystorePassword=prop.getProperty("keystore.password");
			this.keystoreAlias=prop.getProperty("keystore.alias");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

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

		cs = ContentSourceFactory.newContentSource(new URI(xccURL));
		cs.setAuthenticationPreemptive(true);
		Session session = cs.newSession();

		Request request = session.newModuleInvoke("/get-uris-for-transmit-job.xqy");

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
		System.out.println("URIs: " + mlResponse);

		return mlResponse;
	}

	private String getStatusDocument() throws XccConfigException, URISyntaxException, RequestException {
		ContentSource cs;
		cs = ContentSourceFactory.newContentSource(new URI(xccURL));
		cs.setAuthenticationPreemptive(true);
		Session session = cs.newSession();
						
		Request request = session.newModuleInvoke("/build-status-document.xqy");
		ResultSequence rs = session.submitRequest(request);
		return rs.asString();
	}

	private String getDocumentFromDatabase(String uri)
			throws XccConfigException, URISyntaxException, RequestException {
		ContentSource cs;
		cs = ContentSourceFactory.newContentSource(new URI(xccURL));
		cs.setAuthenticationPreemptive(true);
		Session session = cs.newSession();

		Request request = session.newModuleInvoke("/wrap-document.xqy");
		
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

}
