package com.renansouza.processor.config;

import com.renansouza.processor.tasklet.UnzipFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig extends DefaultBatchConfigurer {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private UnzipFiles unzipFiles;

    @Override
    public void setDataSource(final DataSource dataSource) {
        // Do nothing.
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