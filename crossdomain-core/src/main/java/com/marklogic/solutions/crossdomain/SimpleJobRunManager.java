package com.marklogic.solutions.crossdomain;

import java.util.concurrent.ArrayBlockingQueue;

public class SimpleJobRunManager<E> {

    private ArrayBlockingQueue<E> processItemQueue;
    private JobItemsRetriever<E> retriever;
    private JobProcessor<E> processor;

    public SimpleJobRunManager(JobItemsRetriever<E> retriever, JobProcessor<E> processor) {
        this.retriever = retriever;
        this.processor = processor;
    }

    public JobResult runJob() {
        processItemQueue = retriever.retrieve(null);
        processor.setQueue(processItemQueue);
        processor.startJob();
        return processor.jobResult;
    }

}
