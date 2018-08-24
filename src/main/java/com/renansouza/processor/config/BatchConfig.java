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
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
class BatchConfig {

    // Coordenador -> http://www.cherryshoetech.com/2017/10/spring-batch-decision-with-spring-boot.html
    // Splitar o schedluar -> http://walkingtechie.blogspot.com/2017/03/spring-batch-task-scheduler-example.html

    @Value("${chunk-size}")
    private int chunkSize;

    @Value("${max-threads}")
    private int maxThreads;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

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
                .listener(jobExecutionListener())
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step").<Attempt, Attempt>chunk(chunkSize)
                .reader(processAttemptReader())
                .processor(processAttemptProcessor())
                .writer(processAttemptWriter())
                .taskExecutor(taskExecutor())
                .listener(stepExecutionListener())
                .listener(chunkListener())
                .throttleLimit(maxThreads).build();
    }

    // fixedDelayString = "${batch.delay}"
    @Scheduled(fixedRateString = "${batch.rate:60000}"/*, fixedDelayString = "${batch.delay:10000}"*/)
    public void perform() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(job, params);
    }

}