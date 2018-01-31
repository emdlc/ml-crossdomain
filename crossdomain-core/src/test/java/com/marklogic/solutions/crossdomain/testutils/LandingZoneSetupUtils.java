package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.solutions.utils.ClasspathUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class LandingZoneSetupUtils {

    public static void stageTestDataToLandingZone(String testDataFileOrDirLocation, String filesystemLocation) throws IOException {
        ClassLoader classLoader = LandingZoneSetupUtils.class.getClassLoader();


        File testDataDir = ClasspathUtils.getFileOrDirectoryFromClasspath(testDataFileOrDirLocation);
        File lzDir = new File(filesystemLocation);
        lzDir.mkdirs();
        FileUtils.copyDirectory(testDataDir, lzDir);
    }

}
