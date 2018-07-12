package com.renansouza.processor.tasklet;

import com.renansouza.processor.Constants;
import com.renansouza.processor.model.ZIP;
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
import java.io.IOException;

/**
 * <p>
 * {@link org.springframework.batch.core.step.tasklet.Tasklet} implementation
 * to unzip the provided archive.
 * </p>
 */

@Slf4j
@Component
public class UnzipFiles implements Tasklet {

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Autowired
    private
    ZIP zip;

    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
        FileUtils.listFiles(new File(upload), Constants.getCompressedExtensions(), false).forEach(file -> {
            try {
                zip.unzip(file);
                log.info("File {} decompressed successfully.", file.getName());
            } catch (IOException e) {
                log.error("Unable to execute unzip {}.", e.getMessage());
            }
        });

        return RepeatStatus.FINISHED;
    }

}