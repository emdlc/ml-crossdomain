package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.utils.ClasspathUtils;
import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

import org.apache.commons.io.IOUtils;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xml.sax.SAXException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class ReceiveJobProcessor extends JobProcessor<String> {
	
	private int BATCHSIZE;
	private String landingZoneDir;
	private String xccURL;
	private String collection;
	
	@Override
	public JobResult executeJob() {

		Date jobStartDate = new Date();

		while (queue.size() > 0) {
			// get BATCHSIZE uris from queue
			ArrayList<String> filenames = new ArrayList<String>();
			for (int i = 0; (i < BATCHSIZE && queue.size() > 0); i++) {
				String filename = null;
				try {
					filename = queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				filenames.add(filename);
			}

			for (String filename : filenames) {
				//read for each filename on the queue
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(filename);
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}				
				
				//verify content is valid xml
				SAXBuilder builder = new SAXBuilder();
				Document document = null;
				try {
					document = builder.build(fis);
				} catch (JDOMException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Namespace cds = Namespace.getNamespace("p","http://marklogic.com/mlcs/cds");
				Element root = document.getRootElement();
				//get the unique identifier to use as the URI
				String mluri = root.getChildText("UniqueIdentifier", cds);	
				//write the xml document to the database
				String[] coll = new String[] {collection};
				ContentCreateOptions createOptions = new ContentCreateOptions();
				createOptions.setCollections((coll));
				createOptions.setFormatXml();
				DOMOutputter outputter = new DOMOutputter();
				Content content = null;
				try {
					content = ContentFactory.newContent(mluri, outputter.output(document), createOptions);
				} catch (JDOMException e) {
					e.printStackTrace();
				}
				URI uri = null;
				try {
					uri = new URI(xccURL);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				ContentSource source = null;
				try {
					source = ContentSourceFactory.newContentSource(uri);
				} catch (XccConfigException e) {
					e.printStackTrace();
				}
				Session session = source.newSession();
			    try {
					session.insertContent(content);
				} catch (RequestException e) {
					e.printStackTrace();
				}
			}
		// close session

		}
		JobResult jobResult = new JobResult();
		jobResult.setStart(jobStartDate);
		jobResult.setEnd(new Date());
		jobResult.setResultOutput("Completed ReceiveJob");

		return jobResult;
	}

	public ReceiveJobProcessor() {
		super();
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {			
			String filename = "receiveJob.properties";
			input = ReceiveJobProcessor.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null) {
    	            System.out.println("unable to find file:" + filename);
    		    return;
    		}
			
			// load a properties file
			prop.load(input);
			
			this.landingZoneDir=prop.getProperty("landingzone.dir");
			this.xccURL=prop.getProperty("ml.xcc.url");
			this.collection=prop.getProperty("ml.collection");
			this.BATCHSIZE=Integer.parseInt(prop.getProperty("max.batch.count"));
			
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
		File[] listOfFiles = getFilesToReceive();
        Integer n = listOfFiles.length;
		for (int i = 0; i < n; i++) {
			try {
				String fileName = listOfFiles[i].toString();
				queue.put(fileName);
				System.out.println(listOfFiles[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.queue;
}

	private File[] getFilesToReceive() {  //return changed
		File[] listOfFilesToProcess = null;
		try {
			String dir = new String(landingZoneDir);
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();
			listOfFilesToProcess = listOfFiles;
	} catch (Exception e) {
		System.out.println("error");
	}
		return listOfFilesToProcess;
    }	

}

