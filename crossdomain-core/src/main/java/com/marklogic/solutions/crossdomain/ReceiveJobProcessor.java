package com.marklogic.solutions.crossdomain;

import com.marklogic.xcc.*;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.apache.commons.io.FileUtils;
import org.jdom2.input.SAXBuilder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.*;
//import org.assertj.core.util.Files;

public class ReceiveJobProcessor extends JobProcessor<String> {
	
	private int BATCHSIZE;
	private String landingZoneDir;
	private String landingZoneArchiveDir;
	private String xccURL;
	private String collection;
	
	@Override
	public JobResult executeJob() {
		Date jobStartDate = new Date();
		while (queue.size() > 0) {
			for (int i = 0; (i < BATCHSIZE && queue.size() > 0); i++) {
				try {
					String zipFileName = queue.take();
					System.out.println("Processing file: " + zipFileName);
					File file = new File(zipFileName);
					String jarFileFullPath = landingZoneDir + File.separator + file.getName();
					ZipFile zipFile = new ZipFile(jarFileFullPath);
					Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = (ZipEntry) entries.nextElement();
						if (entry.getName().endsWith(".xml")) {
							BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
							//IOUtils.toString(bis, "UTF-8");
							SAXBuilder builder = new SAXBuilder();
							Document document = null;
							document = builder.build(bis);
							Namespace cds = Namespace.getNamespace("p","http://marklogic.com/mlcs/cds");
							Element root = document.getRootElement();
							String mluri = root.getChildText("UniqueIdentifier", cds);
							String[] coll = new String[] {collection};
							ContentCreateOptions createOptions = new ContentCreateOptions();
							createOptions.setCollections((coll));
							createOptions.setFormatXml();
							DOMOutputter outputter = new DOMOutputter();
							Content content = null;	
							content = ContentFactory.newContent(mluri, outputter.output(document), createOptions);
							URI uri = new URI(xccURL);
							ContentSource source = null;
							source = ContentSourceFactory.newContentSource(uri);
							Session session = source.newSession();
							session.insertContent(content);
							bis.close();
						}
					}
					zipFile.close();
					archiveJarFile(file);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (XccConfigException e) {
					e.printStackTrace();
				} catch (RequestException e) {
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
    			System.out.println("unable to find file:" + filename);
    			return;
    		}
			
			// load a properties file
			prop.load(input);
			
			this.landingZoneDir=prop.getProperty("landingzone.dir");
			this.landingZoneArchiveDir=prop.getProperty("landingzone.archive.dir");
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.queue;
}

	private File[] getFilesToReceive() {  
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

	private void archiveJarFile(File incomingJarFile) throws IOException {
		String fileName = incomingJarFile.getName();
		File archivedJarFile = new File(landingZoneArchiveDir, fileName);
		FileUtils.copyFile(incomingJarFile,  archivedJarFile);
		FileUtils.deleteQuietly(incomingJarFile);
	}
}
