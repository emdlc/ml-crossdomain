package com.marklogic.solutions.crossdomain;

import com.marklogic.solutions.crossdomain.jobs.receive.ReceiveItem;
import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.apache.commons.io.FileUtils;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.*;
import org.apache.log4j.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class ReceiveJobProcessor extends JobProcessor<String> {
	
	private int BATCHSIZE;
	private String landingZoneDir;
	private String landingZoneArchiveDir;
	private String landingZoneErrorDir;
	private String xccURL;
	private String collection;
	private Namespace cds = Namespace.getNamespace("p","http://marklogic.com/mlcs/cds");
	private Boolean jarErroredFlag = false; 
	private String jarFileName = null;
	private String jarFileFullPathString = null;
	private static final Logger logger = Logger.getLogger(ReceiveJobProcessor.class);
	
	@Override
	public JobResult executeJob() {
		Date jobStartDate = new Date();
		while (queue.size() > 0) {
			for (int i = 0; (i < BATCHSIZE && queue.size() > 0); i++) {
				String jarFileName;
				try {
					jarFileName = queue.take();
					processZip(jarFileName);
				} catch (IOException e) {
					e.printStackTrace();
				} 
				  catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (XccConfigException | JDOMException | URISyntaxException e) {
					e.printStackTrace();
				}
			}  
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
    			logger.error("unable to find file:" + filename);
    			return;
    		}
			
			// load a properties file
			prop.load(input);
			
			this.landingZoneDir=prop.getProperty("landingzone.dir");
			this.landingZoneArchiveDir=prop.getProperty("landingzone.archive.dir");
			this.landingZoneErrorDir=prop.getProperty("landingzone.error.dir");
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
		if (listOfFiles != null) {
  		    Integer n = listOfFiles.length;
		    for (int i = 0; i < n; i++) {
			    try {
			    	String fileName = listOfFiles[i].toString();
			    	queue.put(fileName);
			    } catch (InterruptedException e) {
			    	e.printStackTrace();
			    }
		    }
		}
		else { 
			logger.info("No files to process on landingzone " + landingZoneDir);
		}
		return this.queue;
	}

	private void processZip(String jarFileFullPath) throws IOException, XccConfigException, JDOMException, URISyntaxException {
		jarErroredFlag = false;
		File file = new File(jarFileFullPath);	
		jarFileName = file.getName();
		jarFileFullPathString = jarFileFullPath;
		try {
			logger.info(jarFileName + " starting processing for " + jarFileFullPath);
			ZipFile zipFile = new ZipFile(jarFileFullPath);
			processFiles(zipFile);
			zipFile.close();
			if (jarErroredFlag == false) {
				moveFile(file, "archive");
				logger.info(jarFileName + " processing successful");
			}
			else {
				logger.info(jarFileName + " processing of contents failed - one or more files in jar failed to ingest");
				moveFile(file, "error");
			}
		} catch (Exception e) {
			logger.error(jarFileName + " failed processing - cannot be opened as a jar file " + jarFileFullPath);
			logger.error(e);
			moveFile(file,"error");
		}
	}

	private void processFiles(ZipFile zipFile) throws XccConfigException, IOException, JDOMException, URISyntaxException {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String entryName = entry.getName();
			if (entry.getName().endsWith(".xml")) {
				xmlValidateAndIngest(zipFile, entry);
			}
			else {
				logger.error(jarFileName + " " + entryName + " ingest failed - not an .xml");
				jarErroredFlag = true;
			}
			
		}
	}

	private void xmlValidateAndIngest(ZipFile zipFile, ZipEntry entry) throws IOException, JDOMException, XccConfigException, URISyntaxException {
			BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
			SAXBuilder builder = new SAXBuilder();
			Document document = null;
			try {
				//fails here with not XML format 
				document = builder.build(bis);
				Element root = document.getRootElement();
				if (root.getNamespacePrefix() == "cds" & 
					 ((root.getName() == "DataEnvelope" & root.getChildText("UniqueIdentifier", cds) != null) ||
					   root.getName() == "StatusEnvelope")) {
						xmlIngest(entry, document, root);							   
					   }				
				else { 
					logger.error(jarFileName + " " + entry + " ingest failed - contains invalid XML schema");
					jarErroredFlag = true;
				}
				bis.close();
			} catch (Exception e) {
				logger.error(jarFileName + " " + entry + " ingest failed - contents not valid XML ");
				logger.error(e);
				jarErroredFlag = true;
		}
	}

	private void xmlIngest(ZipEntry entry, Document document, Element root) throws JDOMException, XccConfigException, URISyntaxException, XPathExpressionException {
		String[] coll = new String[] {collection};
		ContentCreateOptions createOptions = new ContentCreateOptions();
		createOptions.setFormatXml();
		DOMOutputter outputter = new DOMOutputter();
		Content content = null;	

		if (root.getName() == "DataEnvelope") {		
			//assumes src:Data contains the node to insert 
			String d = "Data";
			Element cdsDataElement = root.getChild(d, cds);
			List allChildren = cdsDataElement.getChildren();
			String firstChildName = ((Element)allChildren.get(0)).getName();
			Namespace firstChildNamespace = ((Element)allChildren.get(0)).getNamespace();;
			Element replicatedDoc = cdsDataElement.getChild(firstChildName, firstChildNamespace);
			String[] collection = new String[] {root.getChildText("DomainCollection", cds)};
			createOptions.setCollections((collection));
			content = ContentFactory.newContent(root.getChildText("UniqueIdentifier", cds), outputter.output(replicatedDoc), createOptions);
		} else {
			//assumes only storing most recent status document
			content = ContentFactory.newContent("cds-replication-status", outputter.output(document), createOptions);
		}
		URI uri = new URI(xccURL);
		ContentSource source = null;
		source = ContentSourceFactory.newContentSource(uri);
		Session session = source.newSession();
		try {
			session.insertContent(content);
			logger.info(jarFileName + " " + entry + " ingest successful into database");
		}
		catch (Exception e) {
			logger.error(jarFileName + " " + entry + " ingest failed into database ");
			logger.error(e);
			jarErroredFlag = true;
		}
	}

	private File[] getFilesToReceive() {  
		File[] listOfFilesToProcess = null;
		try {
			String dir = new String(landingZoneDir);
			File folder = new File(dir);
			if (verifyLandingZoneAccessible()) {
				File[] listOfFiles = folder.listFiles();
				listOfFilesToProcess = listOfFiles;
				Arrays.sort(listOfFiles);
				if (listOfFilesToProcess.length == 0) {
					logger.info(landingZoneDir + " no files found to process");
				} else {}
			}
		} catch (Exception e) {
			logger.error(landingZoneDir + " problem reading landingzone");
	}
		return listOfFilesToProcess;
    }	

	private boolean verifyLandingZoneAccessible() {
	    String dir = new String(landingZoneDir);
	    File folder = new File(dir);
    	Boolean dirExists = folder.exists();
	    Boolean isDir = folder.isDirectory();
	    if (dirExists == true & isDir == true) {
	    	return true;
	    }
	    else {
    		logger.error(dir + " landingzone directory does not exist");
    		return false;
	   }
	}
	
	private void moveFile(File file, String location) throws IOException {
		String name = file.getName(); 
		File moveToFile = null;
		if (location == "archive") {
			moveToFile = new File(landingZoneArchiveDir, name);
		} else {
			moveToFile = new File(landingZoneErrorDir, name);
		}
		FileUtils.copyFile(file, moveToFile);
		FileUtils.deleteQuietly(file);
		logger.info(name + " moved to " + location + " dir " + moveToFile);
	}
}
