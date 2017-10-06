package com.marklogic.solutions.crossdomain.jobs.receive.components;

import com.marklogic.solutions.crossdomain.jobs.receive.ReceiveItem;
import org.springframework.batch.item.ItemReader;

public class ReceiveItemReader implements ItemReader<ReceiveItem> {
    @Override
    public ReceiveItem read() throws Exception {
        return null;
    }
}
