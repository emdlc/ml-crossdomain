package com.marklogic.solutions.crossdomain;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public interface JobItemsRetriever<E> {

    ArrayBlockingQueue<E> retrieve(Map<String, Object> params);

}
