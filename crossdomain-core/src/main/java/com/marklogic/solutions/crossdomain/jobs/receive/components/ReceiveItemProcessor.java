package com.marklogic.solutions.crossdomain.jobs.receive.components;

import com.marklogic.solutions.crossdomain.jobs.receive.ReceiveItem;
import org.springframework.batch.item.ItemProcessor;

public class ReceiveItemProcessor implements ItemProcessor<ReceiveItem, ReceiveItem> {
    @Override
    public ReceiveItem process(ReceiveItem item) throws Exception {
        return null;
    }
}
