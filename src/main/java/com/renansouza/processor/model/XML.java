package com.renansouza.processor.model;

import lombok.Getter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XML extends Attempt {

    // https://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html

    @Getter
    private String doc;

    public XML(File file) {
        super(file);
        this.doc = identifyDoc(file);
    }

    private String identifyDoc(File file) {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder parser= null;
        try {
            parser = factory.newDocumentBuilder();
            Document doc=parser.parse(new FileInputStream(file));
            return doc.getDocumentElement().getTagName();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Não rolou");
        }
    }
}