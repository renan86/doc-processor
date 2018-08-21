package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.item.ItemReader;

import java.io.File;

public class Reader implements ItemReader<XML> {

    @Override
    public XML read() {

        XML xml = null;
        val file = FileUtils.listFiles(new File("file/upload"), new String[]{"xml"}, false).stream().findFirst();

        if (file.isPresent()) {
            System.out.println("Lendo o arquivo " + file.get().getName());
            xml = new XML(file.get());
        }

        return xml;

    }
}
