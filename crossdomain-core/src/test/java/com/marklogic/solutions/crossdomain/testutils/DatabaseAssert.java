package com.marklogic.solutions.crossdomain.testutils;

import org.junit.Assert;

public class DatabaseAssert extends Assert {

    DatabaseTestUtils dbUtils;

    public DatabaseAssert(String xccUrl) {
        dbUtils = new DatabaseTestUtils(xccUrl);
    }


}
