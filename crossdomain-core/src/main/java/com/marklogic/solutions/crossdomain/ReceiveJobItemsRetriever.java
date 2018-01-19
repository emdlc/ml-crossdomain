package com.marklogic.solutions.crossdomain;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class ReceiveJobItemsRetriever {

	private static final String landingZoneDir = "c:/Temp/landingzone/low2high/";
	
	private ArrayBlockingQueue<String> queue;
	
	public ReceiveJobItemsRetriever() {
		this.queue = new ArrayBlockingQueue<String>(10000);
	}


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
