package com.renansouza.processor.config;

import com.renansouza.processor.Constants;
import com.renansouza.processor.config.domain.zip.Zip;
import com.renansouza.processor.util.Decompress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class UnzipFiles implements Tasklet {

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Autowired
    private Decompress decompress;

    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        FileUtils.listFiles(new File(upload), Constants.getCompressedExtensions(), false).forEach(file -> {
            decompress.unzip(new Zip(file));
            log.info("File {} decompressed successfully.", file.getName());
        });

        return RepeatStatus.FINISHED;
    }

}
