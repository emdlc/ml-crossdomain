package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.solutions.utils.ClasspathUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.springframework.util.FileSystemUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

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
        // TODO: Implement
    }

    public Collection<String> getJarFilesInLandingZone() {
        ArrayList<String> jarFileNames = new ArrayList<String>();
        // TODO: Implement
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
