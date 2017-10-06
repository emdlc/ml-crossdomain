package com.marklogic.solutions.crossdomain.jobs.receive;

import com.marklogic.solutions.crossdomain.jobs.receive.components.ReceiveItemProcessor;
import com.marklogic.solutions.crossdomain.jobs.receive.components.ReceiveItemReader;
import com.marklogic.solutions.crossdomain.jobs.receive.components.ReceiveItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class ReceiveJobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    ReceiveItemReader reader;

    ReceiveItemWriter writer;

    ReceiveItemProcessor processor;

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
                .<ReceiveItem, ReceiveItem>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

}
