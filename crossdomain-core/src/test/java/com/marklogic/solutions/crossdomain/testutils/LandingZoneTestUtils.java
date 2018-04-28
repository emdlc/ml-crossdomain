package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.solutions.utils.ClasspathUtils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LandingZoneTestUtils extends Assert {

    private static final Logger logger = Logger.getLogger(LandingZoneTestUtils.class);
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

    public boolean filePathExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public void assertFileExists(String lzRelativePath) {
        assertTrue(String.format("'%s' file does not exist in landingzone", lzRelativePath), fileExists(lzRelativePath));
    }

    public void assertFilePathExists(String filePath) {
        assertTrue(String.format("'%s' file does not exist in archive ", filePath), filePathExists(filePath));
    }

    public void assertJarFilesInLandingZone(int count) {
        int total = getJarFilesInLandingZone().size();
        assertEquals(count, total);
    }

    public void assertContentFileForUriInJar(String contentFileNameToFind) {
        ArrayList<String> jarFileNames = (ArrayList<String>) getJarFilesInLandingZone();
        String contentJarName = null;
        for (String name : jarFileNames) {
            if (name.indexOf("status") == -1) {
                // this is the content file
                contentJarName = name;
            }
        }
        System.out.println(contentJarName);
        boolean jarContainsContentFile = false;

        //get the zip file content
        ZipInputStream zis;
        try {
            if (!fileExists(contentJarName)) {
                return;
            }
            zis = new ZipInputStream(new FileInputStream(lzPath + "/" + contentJarName));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                ze = zis.getNextEntry();
                System.out.println(fileName);
                if (fileName.equals(contentFileNameToFind)) {
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
        for (int i = 0; i < fileNames.length; i++) {
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
    
	private int verifyJar(String jarFilePath) throws IOException, InterruptedException {
		String [] commands = {"jarsigner", 
								"-verbose",
								"-verify", 
								jarFilePath, "CDS"};

		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		Process p = processBuilder.redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
		p.waitFor();
		int exitCode = p.exitValue();
		System.out.println("JAR Verification exit code="+exitCode);
		return exitCode;
	}

    public void assertJarIsSigned() {    	    	
    	
        ArrayList<String> jarFileNames = (ArrayList<String>) getJarFilesInLandingZone();
        String contentJarName = null;
        for (String name : jarFileNames) {
            if (name.indexOf("status") == -1) {
                // this is the content file
                contentJarName = name;
            }
        }
        System.out.println(contentJarName);

        boolean isJarSigned = true;
        boolean isManifestPresent = false;
        JarFile jar = null;
        int exitCode = 0;
		try {
			jar = new JarFile(lzPath + "/" + contentJarName);
			exitCode = verifyJar(lzPath + "/" + contentJarName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            System.out.println("JarEntryName:"+entry.getName());
            if(entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
            	isManifestPresent = true;
            }
            try {
                byte[] buffer = new byte[8192];
                InputStream is = jar.getInputStream(entry);
                while ((is.read(buffer, 0, buffer.length)) != -1) {
                    // Just read the file. Will throw a SecurityException if a signature/digest check fails.
                }
            } catch (SecurityException se) {
            	isJarSigned = false;
                assertTrue("Jar is not properly signed", isJarSigned);
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        assertTrue("A JAR file without a manifest should not pass", isManifestPresent);
        assertEquals("Jarsigner verification exitcode indicates JAR is not properly signed", exitCode, 0);
    }

    public void assertFileComeInOrder() {
        try {
            logger.info("Starts LZ assertion");
            File[] files = lzDir.listFiles();

            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
            for (File jar : files) {
                ZipInputStream zis = new ZipInputStream(new FileInputStream(jar));
                ZipEntry entry = null;
                Integer lastFileNumber = 0;
                while ((entry = zis.getNextEntry()) != null) {
                    logger.info("ZipEntry=" + entry.getName());

                    if (entry.getName().endsWith("data-status.xml")) {
                        // TODO: Verification of data
                    } else { 
                    	if(		entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF") ||
                    			entry.getName().endsWith(".SF") ||
                    			entry.getName().endsWith(".DSA") ) {
                    			// ignore these are files related to the JAR being signed
                    	} else {                    	
	                        assertTrue(String.format("Filename '%s' doesn't match data", entry.getName()), entry.getName().matches("/data/\\d+\\.xml"));
	                        Integer newLastFileNumber = Integer.valueOf(entry.getName().replace("/data/", "").replace(".xml", ""));
	                        assertTrue("Files not in order at integer " + newLastFileNumber, newLastFileNumber > lastFileNumber);
	                        lastFileNumber = newLastFileNumber;
	                    }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
