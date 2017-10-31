package com.marklogic.solutions.crossdomain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

public class ReceiveJobItemsRetriever implements JobItemsRetriever<String>{

	private static final String landingZoneDir = "c:/Temp/landingzone/low2high/";
	
	private ArrayBlockingQueue<String> queue;
	
	public ReceiveJobItemsRetriever() {
		this.queue = new ArrayBlockingQueue<String>(10000);
	}

	@Override
	public ArrayBlockingQueue<String> retrieve(Map<String, Object> params) {
		// get files and add them to the queue
		System.out.println("Testing");
		String fileList = getFilesToReceive();
		String[] fileNames = fileList.split("\n");
		for (int i = 0; i < fileList.length(); i++) {
			try {
				queue.put(fileNames[i]);
				System.out.println(fileNames[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.queue;
	}
	
	private String getFilesToReceive() {
        System.out.println("123");
		String listOfFiles = null;
		try {
			String dir = new String("c:/Temp/landingzone/low2high/");
			File folder = new File(dir);
			listOfFiles = folder.toString();
	} catch (Exception e) {
		System.out.println("error");
	}
		return listOfFiles;
		
	}
}
