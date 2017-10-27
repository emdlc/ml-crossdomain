package com.marklogic.solutions.crossdomain.jobs.receive;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.exceptions.XccException;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.Session;

public class ReceiveItem {
	
	static Logger log = Logger.getLogger(ReceiveItem.class);
	private static final int corePoolSize = 2;
	private static final int maxPoolSize = corePoolSize;
	private static final long keepAliveTime = 10;
	private static BlockingQueue<Runnable> queue;
	private static ThreadPoolExecutor pool;
	
	protected static ContentSource source;
	
	public static void WriteFile(Document document, String mluri) 
			throws XccConfigException, URISyntaxException, RequestException, 
			       ParserConfigurationException, SAXException, IOException {
		String[] collection = new String[] {"low2high"};
		ContentCreateOptions createOptions = new ContentCreateOptions();
		createOptions.setCollections(collection);
		createOptions.setFormatXml();
		DOMOutputter outputter = new DOMOutputter();
		Content content = null;
		try {
			content = ContentFactory.newContent(mluri, outputter.output(document), createOptions);
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		log.info("WriteFile starting ML insert for " + mluri);
		URI uri = new URI("xcc://admin:password@localhost:8201");
		ContentSource source = ContentSourceFactory.newContentSource(uri);
		Session session = source.newSession();
	    session.insertContent(content);
		log.info("WriteFile completed ML insert for " + mluri);
	}
	
	public static void ProcessFile(FileInputStream fis) 
			throws XccConfigException, RequestException,
			       URISyntaxException, ParserConfigurationException, 
			       SAXException, IOException, JDOMException{
		//verify content is valid xml
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		document = builder.build(fis);
		log.info("ProcessFile xml format valid ");
		Namespace cds = Namespace.getNamespace("p","http://marklogic.com/mlcs/cds");
		Element root = document.getRootElement();
		String mluri = root.getChildText("UniqueIdentifier", cds);	
		try {
			WriteFile(document, mluri);
		} catch (XccConfigException | RequestException | URISyntaxException | ParserConfigurationException
				| SAXException | IOException e) {
			e.printStackTrace();
		}
	}
		
	public static class ReadFile implements Runnable  {
		private File file;
		
		public ReadFile(File file) {
			this.file = file;
		} 
		
		@Override
		public void run() {
			Long threadId = Thread.currentThread().getId();
			log.info("ReadFile thread " + threadId + " reading file " + file.getName());
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			try {
				ProcessFile(fis);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (XccConfigException e) {
				e.printStackTrace();
			} catch (RequestException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			}
		}
	}
			
	public static class SimpleRejectedExecutionHandler implements RejectedExecutionHandler {
		public SimpleRejectedExecutionHandler() {
		}
		@Override
		public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
		}
	}
	
	public static void main(String[] args) throws XccException, URISyntaxException, IOException, JDOMException {
		BasicConfigurator.configure();
		long startTime = System.currentTimeMillis();
		log.info("Starting " + startTime);
		queue = new LinkedBlockingQueue<Runnable>();
		RejectedExecutionHandler handler = new ReceiveItem.SimpleRejectedExecutionHandler();
		pool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue, handler);
		pool.prestartAllCoreThreads();
		try {
			String dir = new String("c:/Temp/landingzone/low2high/");
			log.info("Reading directory " + dir);
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				if (file.isFile()) {
						pool.execute(new ReadFile(file));
				}
			}
		pool.shutdown();
		while (!pool.awaitTermination(1,  TimeUnit.SECONDS))
				log.info("Waiting for pool to terminate, active threads: " + pool.getActiveCount());
			
		long endTime = System.currentTimeMillis();
		long durationTime = endTime - startTime;
		log.info("Stopping " + endTime); 
		log.info("Duration ms " + durationTime);
		} catch (Exception e) {
			System.out.println("error");
		}
	}
}
