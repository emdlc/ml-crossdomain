package com.marklogic.solutions.crossdomain.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by edelacruz on 6/7/17.
 */
@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = {"com.marklogic.solutions.crossdomain.services"})
public class CoreConfiguration {



}
