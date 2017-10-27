package com.marklogic.solutions.crossdomain;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class SimpleJobTest {

    class NumberJobItem {
        public int number;

        public NumberJobItem(int number) {
            this.number = number;
        }
    }

    class NumberJobItemsRetriever implements JobItemsRetriever<NumberJobItem> {

        ArrayBlockingQueue<NumberJobItem> numberQueue = new ArrayBlockingQueue<NumberJobItem>(10);

        @Override
        public ArrayBlockingQueue<NumberJobItem> retrieve(Map<String, Object> params) {
            numberQueue.add(new NumberJobItem(6));
            numberQueue.add(new NumberJobItem(5));
            numberQueue.add(new NumberJobItem(4));
            numberQueue.add(new NumberJobItem(3));
            return numberQueue;
        }
    }

    class AddOneJob extends JobProcessor<NumberJobItem> {

        public AddOneJob() {
        }

        @Override
        public JobResult executeJob() {
            String result = "";
            for (NumberJobItem item : queue) {
                result += (item.number + 1) + "\n";
            }
            jobResult.setResultOutput(result);
            return jobResult;
        }
    }

    @Test
    public void runAddOneJob() {
        SimpleJobRunManager mgr = new SimpleJobRunManager(new NumberJobItemsRetriever(), new AddOneJob());
        JobResult result = mgr.runJob();
        System.out.println("result=" + result.getResultOutput());
    }

}
