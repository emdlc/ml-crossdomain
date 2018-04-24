package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.solutions.utils.ClasspathUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LandingZoneTestUtils extends Assert {

    public File lzDir;
    public String lzPathStr;
    public Path lzPath;

    public LandingZoneTestUtils(String fileSystemPath) {
        lzDir = new File(fileSystemPath);
        lzDir.mkdirs();
        lzPathStr = fileSystemPath;
        lzPath = Paths.get(lzPathStr);
    }

    public boolean fileExists(String lzRelativePath) {
        return Files.exists(Paths.get(lzPath + "/" + lzRelativePath));
    }

    public void assertFileExists(String lzRelativePath) {
        assertTrue(String.format("'%s' file does not exist in landingzone", lzRelativePath), fileExists(lzRelativePath));
    }

    public void assertJarFilesInLandingZone(int count) {
    	int total = getJarFilesInLandingZone().size();
    	assertEquals(count, total);
    }
    
	public void assertContentFileForUriInJar(String contentFileNameToFind) {
		ArrayList<String> jarFileNames = (ArrayList<String>) getJarFilesInLandingZone();
		String contentJarName = null;
		for(String name : jarFileNames) {
			if(name.indexOf("status") == -1) {
				// this is the content file
				contentJarName = name;
			}
		}
		System.out.println(contentJarName);
		boolean jarContainsContentFile = false;
		
		//get the zip file content
        ZipInputStream zis;
		try {
			if(!fileExists(contentJarName)) {
				return;
			}
			zis = new ZipInputStream(new FileInputStream(lzPath + "/" + contentJarName));
	        //get the zipped file list entry
	        ZipEntry ze = zis.getNextEntry();
	        while(ze != null) {
	           String fileName = ze.getName();
	           ze = zis.getNextEntry();
	           System.out.println(fileName);
	           if(fileName.equals(contentFileNameToFind)) {
	        	   jarContainsContentFile = true;
	        	   break;
	           }
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Jar file does not contain content file.", jarContainsContentFile);
	}
		

    public Collection<String> getJarFilesInLandingZone() {
        ArrayList<String> jarFileNames = new ArrayList<String>();

    	File folder = new File(this.lzPathStr);
    	String[] fileNames = folder.list();
    	for (int i = 0; i< fileNames.length; i++)
    	{
    	  if (fileNames[i].contains(".jar"))
    		  jarFileNames.add(fileNames[i]);
    	}        
        return jarFileNames;
    }

    public String extractFileContentsInJar(String relativeJarPath, String filePathInJar) {
        String contents = null;
        // TODO: Implement
        return contents;
    }

    public void clearLandingZone() {
        FileSystemUtils.deleteRecursively(lzDir);
        lzDir.mkdirs();
    }

    public void stageTestDataToLandingZone(String testDataFileOrDirLocation) throws IOException {

        File testDataDir = ClasspathUtils.getFileOrDirectoryFromClasspath(testDataFileOrDirLocation);
        FileUtils.copyDirectory(testDataDir, lzDir);
    }

}
