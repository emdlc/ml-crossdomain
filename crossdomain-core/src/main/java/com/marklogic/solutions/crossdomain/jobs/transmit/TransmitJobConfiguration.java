package com.marklogic.solutions.crossdomain.jobs.transmit;

import com.marklogic.solutions.crossdomain.jobs.transmit.components.TransmitItemProcessor;
import com.marklogic.solutions.crossdomain.jobs.transmit.components.TransmitItemReader;
import com.marklogic.solutions.crossdomain.jobs.transmit.components.TransmitItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class TransmitJobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    TransmitItemReader reader;

    TransmitItemWriter writer;

    TransmitItemProcessor processor;

    @Bean
    public Job receiveJob() {
        return jobBuilderFactory.get("receiveJob")
                .incrementer(new RunIdIncrementer())
//                .listener(listener)
                .flow(receiveStep())
                .end()
                .build();
    }

    @Bean
    public Step receiveStep() {
        return stepBuilderFactory.get("receiveStep")
                .<TransmitItem, TransmitItem>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
