package com.renansouza.processor.util;

import com.renansouza.processor.Constants;
import com.renansouza.processor.config.domain.zip.Zip;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DecompressIT {

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Autowired
    private Decompress decompress;

    @Before
    public void copyTestFiles() {
        FileUtils.listFiles(new File("src/test/resources"), Constants.getCompressedExtensions(), false).forEach(file -> {
            try {
                if (!Paths.get(upload).toFile().exists()) {
                    Files.createDirectories(Paths.get(upload));
                }

                FileUtils.copyFile(file, new File(upload + File.separator + "Nfe_Producao;Recebimento;" + file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void shouldDecompressSomeFiles() {
        FileUtils.listFiles(new File(upload), Constants.getCompressedExtensions(), false).forEach(file -> decompress.unzip(new Zip(file)));

        Assert.assertEquals(0, FileUtils.listFiles(new File(upload), Constants.getCompressedExtensions(), false).size());

        try {
            Map<String, Long> result = Files
                    .list(Paths.get(upload))
                    .filter(p -> p.toFile().getName().endsWith(".xml"))
                    .collect(groupingBy(p -> StringUtils.substringBeforeLast(p.toString(), ";").replace("file" + File.separator + "upload" + File.separator, ""), counting()));

            Assert.assertEquals("[{Nfe_Producao;Recebimento=4}]", Collections.singletonList(result).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}