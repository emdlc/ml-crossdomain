package com.marklogic.solutions.crossdomain;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class JobProcessor<E> {

    public void setQueue(ArrayBlockingQueue<E> queue) {
        this.queue = queue;
    }

    protected ArrayBlockingQueue<E> queue;
    protected JobResult jobResult = new JobResult();

    public abstract JobResult executeJob();

    protected abstract ArrayBlockingQueue<E> retrieve(Map<String, Object> params);

    public void loadQueue(Map<String, Object> params) {
        queue = retrieve(params);
    }

    public JobProcessor() {
        this.queue = new ArrayBlockingQueue<E>(10000);
    }


}
