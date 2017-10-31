package com.marklogic.solutions.utils;

import java.io.InputStream;

public class ClasspathUtils {

    public static InputStream getClasspathContentAsStream(String path) {
        return ClasspathUtils.class.getResourceAsStream(path);
    }

}
