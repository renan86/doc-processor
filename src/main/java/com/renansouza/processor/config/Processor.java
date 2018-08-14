package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import org.springframework.batch.item.ItemProcessor;

public class Processor implements ItemProcessor<XML, XML> {

    @Override
    public XML process(XML xml) {
        System.out.println("Passou pelo process");
        return xml;
    }

}