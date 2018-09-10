package com.renansouza.processor.config;

import com.renansouza.processor.config.domain.Attempt;
import com.renansouza.processor.config.domain.xml.Xml;
import com.renansouza.processor.config.domain.zip.Zip;
import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.LinkedList;
import java.util.Queue;

@Configuration
public class CommonQueues {

    public static final Queue<Attempt> attemptQueue = new LinkedList<>();
    public static final Queue<Xml> xmlQueue = new LinkedList<>();
    public static final Queue<Zip> zipQueue = new LinkedList<>();


}