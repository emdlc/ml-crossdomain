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


    class AddOneJob extends JobProcessor<NumberJobItem> {

        public AddOneJob() {
        }

        @Override
        public ArrayBlockingQueue<NumberJobItem> retrieve(Map<String, Object> params) {
            queue.add(new NumberJobItem(6));
            queue.add(new NumberJobItem(5));
            queue.add(new NumberJobItem(4));
            queue.add(new NumberJobItem(3));
            return queue;
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
        SimpleJobRunManager mgr = new SimpleJobRunManager(new AddOneJob());
        JobResult result = mgr.runJob();
        System.out.println("result=" + result.getResultOutput());
    }

}
