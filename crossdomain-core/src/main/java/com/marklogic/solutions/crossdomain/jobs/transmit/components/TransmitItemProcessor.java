package com.marklogic.solutions.crossdomain.jobs.transmit.components;

import com.marklogic.solutions.crossdomain.jobs.transmit.TransmitItem;
import org.springframework.batch.item.ItemProcessor;

public class TransmitItemProcessor implements ItemProcessor<TransmitItem, TransmitItem> {
    @Override
    public TransmitItem process(TransmitItem item) throws Exception {
        return null;
    }
}
