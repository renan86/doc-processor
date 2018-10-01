package com.renansouza.processor.config.domain.xml;

import com.renansouza.processor.config.domain.Attempt;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class Xml extends Attempt {

    @Getter
    private final String docType;

    public Xml(File file) {
        super(file);
        this.docType = identifyDoc(file);
    }

    private String identifyDoc(File file) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser;
        try {
            parser = factory.newDocumentBuilder();
            Document document = parser.parse(new FileInputStream(file));
            return document.getDocumentElement().getTagName();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Error while parsing/reading given file {}: {}", file.getName(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}