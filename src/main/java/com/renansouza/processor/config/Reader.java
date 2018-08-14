package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import org.springframework.batch.item.ItemReader;

public class Reader implements ItemReader<XML> {

    @Override
    public XML read() {
        System.out.println("Passou pelo read");
        return null;
    }
}
