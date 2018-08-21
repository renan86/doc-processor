package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import com.renansouza.processor.tasklet.UnzipFiles;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public UnzipFiles unzipFiles;

    @Bean
    public Job processJob() {
        return jobBuilderFactory
                .get("processJob")
                .incrementer(new RunIdIncrementer())
                .flow(orderStep1())
                .end()
                .build();
    }

    @Bean
    public Step orderStep1() {
        return stepBuilderFactory
                .get("orderStep1")
                .<XML, XML> chunk(1)
                .reader(new Reader())
                .processor(new Processor())
                .writer(new Writer())
                .build();
    }

    @Bean
    public Job unzipJob() {
        return jobBuilderFactory.get("unzipJob").flow(unzip()).end().build();
    }

    @Bean
    public Step unzip() {
        return stepBuilderFactory.get("unzip").tasklet(unzipFiles).build();
    }

}