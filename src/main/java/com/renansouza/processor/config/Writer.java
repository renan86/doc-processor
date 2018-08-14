package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class Writer implements ItemWriter<XML> {

    @Override
    public void write(List<? extends XML> list) {
        System.out.println("Passou pelo writer");
    }

}