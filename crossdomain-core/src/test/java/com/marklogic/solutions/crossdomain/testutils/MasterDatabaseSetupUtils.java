package com.marklogic.solutions.crossdomain.testutils;

import com.marklogic.solutions.utils.ClasspathUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class MasterDatabaseSetupUtils {

    DatabaseTestUtils dbUtils;

    public MasterDatabaseSetupUtils(String xccUrl) {
        dbUtils = new DatabaseTestUtils(xccUrl);
    }

    public static void stageTestDataToDatabase(String xccUrl, String filesystemLocation) throws IOException {
        ClassLoader classLoader = MasterDatabaseSetupUtils.class.getClassLoader();


        File testDataDir = ClasspathUtils.getFileOrDirectoryFromClasspath(xccUrl);
        File lzDir = new File(filesystemLocation);
        lzDir.mkdirs();
        FileUtils.copyDirectory(testDataDir, lzDir);
    }

}
