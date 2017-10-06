package com.marklogic.solutions.crossdomain.services;

import com.marklogic.solutions.crossdomain.config.CoreConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by edelacruz on 6/8/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CoreConfiguration.class})
public class CorePropertiesTest {

    private static final Logger logger = Logger.getLogger(CorePropertiesTest.class);

    @Autowired
    private CoreProperties props;

    @Test
    public void testValue() {
        logger.info("sourcePort=" + props.getSourcePort());
    }

}
