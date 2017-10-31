package com.marklogic.solutions.crossdomain;

import java.util.concurrent.ArrayBlockingQueue;

public class SimpleJobRunManager<E> {

    private ArrayBlockingQueue<E> processItemQueue;
    private JobProcessor<E> processor;

    public SimpleJobRunManager(JobProcessor<E> processor) {
        this.processor = processor;
    }

    public JobResult runJob() {

        processor.loadQueue(null);
        JobResult jobResult = processor.executeJob();
        return jobResult;
    }

}
