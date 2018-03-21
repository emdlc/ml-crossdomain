package com.marklogic.solutions.crossdomain.testutils;

import org.junit.Assert;

public class LandingZoneAssert extends Assert {

    String directoryLocation;

    public LandingZoneAssert(String directoryLocation) {
        this.directoryLocation = directoryLocation;
    }


}
