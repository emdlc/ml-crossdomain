package com.marklogic.solutions.crossdomain.jobs.transmit.components;

import com.marklogic.solutions.crossdomain.jobs.transmit.TransmitItem;
import org.springframework.batch.item.ItemReader;

public class TransmitItemReader implements ItemReader<TransmitItem> {
    @Override
    public TransmitItem read() throws Exception {
        return null;
    }
}
