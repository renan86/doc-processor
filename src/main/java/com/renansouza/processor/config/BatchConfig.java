package com.renansouza.processor.config;

import com.renansouza.processor.tasklet.UnzipFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private UnzipFiles unzipFiles;

    @Bean
    public Job unzipJob() {
        return jobBuilderFactory.get("unzipJob").flow(unzip()).end().build();
    }

    @Bean
    public Step unzip() {
        return stepBuilderFactory.get("unzip").tasklet(unzipFiles).build();
    }

}