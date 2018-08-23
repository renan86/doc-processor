package com.renansouza.processor.config;

import com.renansouza.processor.config.attempt.Attempt;
import com.renansouza.processor.config.attempt.AttemptProcessor;
import com.renansouza.processor.config.attempt.AttemptReader;
import com.renansouza.processor.config.attempt.AttemptWriter;
import com.renansouza.processor.config.listener.ChunkExecutionListener;
import com.renansouza.processor.config.listener.JobCompletionNotificationListener;
import com.renansouza.processor.config.listener.StepExecutionNotificationListener;
import com.renansouza.processor.tasklet.UnzipFiles;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class BatchConfig {

    @Value("${chunk-size}")
    private int chunkSize;

    @Value("${max-threads}")
    private int maxThreads;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public UnzipFiles unzipFiles;

    @Bean
    public AttemptReader processAttemptReader() {
        return new AttemptReader();
    }

    @Bean
    public AttemptProcessor processAttemptProcessor() {
        return new AttemptProcessor();
    }

    @Bean
    public AttemptWriter processAttemptWriter() {
        return new AttemptWriter();
    }

    @Bean
    public JobCompletionNotificationListener jobExecutionListener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public StepExecutionNotificationListener stepExecutionListener() {
        return new StepExecutionNotificationListener();
    }

    @Bean
    public ChunkExecutionListener chunkListener() {
        return new ChunkExecutionListener();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(maxThreads);
        return taskExecutor;
    }

    @Bean
    public Job processAttemptJob() {
        return jobBuilderFactory.get("process-attempt-job")
                .incrementer(new RunIdIncrementer())
//                .listener(jobExecutionListener())
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step").<Attempt, Attempt>chunk(chunkSize)
                .reader(processAttemptReader())
                .processor(processAttemptProcessor())
                .writer(processAttemptWriter())
                .taskExecutor(taskExecutor())
//                .listener(stepExecutionListener())
//                .listener(chunkListener())
                .throttleLimit(maxThreads).build();
    }

}