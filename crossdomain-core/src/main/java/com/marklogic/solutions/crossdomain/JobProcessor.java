package com.marklogic.solutions.crossdomain;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class JobProcessor<E> {

    public void setQueue(ArrayBlockingQueue<E> queue) {
        this.queue = queue;
    }

    protected ArrayBlockingQueue<E> queue;
    protected JobResult jobResult = new JobResult();

    public abstract JobResult executeJob();

    public JobProcessor() {
    }


}
