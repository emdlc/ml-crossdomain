package com.marklogic.solutions.crossdomain.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by edelacruz on 6/8/17.
 */
@Component
public class CoreProperties {

    @Value("${ml.crossdomain.source.host:localhost}")
    public String sourceHostname;

    public CoreProperties() {

    }

    public String getSourceHostname() {
        return sourceHostname;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setSourceHostname(String sourceHostname) {
        this.sourceHostname = sourceHostname;
    }

    @Value("${ml.crossdomain.source.port:8002}")
    private String sourcePort;


}
