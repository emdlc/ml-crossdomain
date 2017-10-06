package com.marklogic.solutions.crossdomain.jobs.transmit.components;

import com.marklogic.solutions.crossdomain.jobs.transmit.TransmitItem;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class TransmitItemWriter implements ItemWriter<TransmitItem> {
    @Override
    public void write(List<? extends TransmitItem> items) throws Exception {

    }
}
