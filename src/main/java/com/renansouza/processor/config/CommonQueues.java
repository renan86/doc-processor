package com.renansouza.processor.config;

import com.renansouza.processor.config.domain.Attempt;
import com.renansouza.processor.config.domain.xml.Xml;
import com.renansouza.processor.config.domain.zip.Zip;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.Queue;

@Configuration
public class CommonQueues {

    @Getter
    private static final Queue<Attempt> attemptQueue = new LinkedList<>();
    @Getter
    private static final Queue<Xml> xmlQueue = new LinkedList<>();
    @Getter
    private static final Queue<Zip> zipQueue = new LinkedList<>();

}