package com.marklogic.solutions.crossdomain.jobs.receive.components;

import com.marklogic.solutions.crossdomain.jobs.receive.ReceiveItem;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class ReceiveItemWriter implements ItemWriter<ReceiveItem> {
    @Override
    public void write(List<? extends ReceiveItem> items) throws Exception {

    }
}
