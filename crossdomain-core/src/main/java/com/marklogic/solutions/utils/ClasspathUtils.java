package com.marklogic.solutions.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClasspathUtils {

    public static InputStream getClasspathContentAsStream(String path) {
        return ClasspathUtils.class.getResourceAsStream(path);
    }

    public static File getFileOrDirectoryFromClasspath(String path) {
        return new File(ClasspathUtils.class.getResource(path).getFile());
    }

    public static Properties getPropertiesFileFromClasspath(String path) throws IOException {
        Properties properties = new Properties();
        properties.load(getClasspathContentAsStream(path));
        return properties;
    }

}
