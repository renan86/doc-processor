package com.renansouza.processor.file;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileControllerIT {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        Assert.assertTrue(new File("src/test/resources/Files.zip").exists());

        MockMultipartFile multipartFile = new MockMultipartFile("file", "Files.zip", "", new FileInputStream(new File("src/test/resources/Files.zip")).readAllBytes());

        this.mvc.perform(MockMvcRequestBuilders.multipart("/upload").file(multipartFile)
            .param("env", "1")
            .param("flow","1"))
                .andExpect(status().isOk())
                .andExpect(status().is(200))
                .andExpect(content().string("Successfully uploaded files."));
    }

}
