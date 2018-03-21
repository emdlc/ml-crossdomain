package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.solutions.utils.ClasspathUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public boolean fileExists(String relativePath) {
        return Files.exists(Paths.get(lzPath + "/" + relativePath));
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
