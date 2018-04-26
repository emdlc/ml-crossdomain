package com.marklogic.solutions.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClasspathUtils {

    public static InputStream getClasspathContentAsStream(String path) {
        return ClasspathUtils.class.getResourceAsStream(path);
    }

    public static String getResourceContentsAsString(String path) throws IOException {
        return IOUtils.toString(getClasspathContentAsStream(path), "UTF-8");
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
