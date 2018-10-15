package com.renansouza.processor.xml;

import com.renansouza.processor.config.domain.xml.Xml;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XmlIT {

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Test
    public void shouldParseFile() {
        FileUtils.listFiles(new File(upload), new String[]{"xml"}, false).forEach(file -> {
            try {
                Assert.assertEquals("nfeProc", new Xml(file).getDocType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
