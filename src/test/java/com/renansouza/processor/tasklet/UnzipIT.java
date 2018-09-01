package com.renansouza.processor.tasklet;

import com.renansouza.processor.Constants;
import com.renansouza.processor.model.ZIP;
import junitparams.JUnitParamsRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;


@RunWith(JUnitParamsRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UnzipIT {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ZIP zip;

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Before
    public void build() throws IOException {
        if (!Paths.get(upload).toFile().exists()) {
            Files.createDirectories(Paths.get(upload));
        }
    }

    @Before
    public void copyTestFiles() {
        FileUtils.listFiles(new File("src/test/resources"), Constants.getCompressedExtensions(), false).forEach(file -> {
            try {
                FileUtils.copyFile(file, new File(upload + File.separator + "Nfe_Producao;Recebimento;" + file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void unzipFile() throws IOException {
        FileUtils.listFiles(new File(upload), Constants.getCompressedExtensions(), false).forEach(file -> {
            try {
                zip.unzip(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        var count = FileUtils.listFiles(new File(upload), Constants.getCompressedExtensions(), false).size();
        Assert.assertEquals(0, count);

        Map<String, Long> result = Files
                .list(Paths.get(upload))
                .filter(p -> p.toFile().getName().endsWith(".xml"))
                .collect(groupingBy(p -> StringUtils.substringBeforeLast(p.toString(), ";").replace("file\\upload\\", ""), counting()));

        Assert.assertEquals("[{Nfe_Producao;Recebimento=4}]", Collections.singletonList(result.toString().replace(upload + "/", "")).toString());
    }
}