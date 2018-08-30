package com.renansouza.processor.config;

import com.renansouza.processor.config.listener.ChunkExecutionListener;
import com.renansouza.processor.config.listener.JobCompletionNotificationListener;
import com.renansouza.processor.config.listener.StepExecutionNotificationListener;
import com.renansouza.processor.config.xml.XMLProcessor;
import com.renansouza.processor.config.xml.XMLReader;
import com.renansouza.processor.config.xml.XMLWriter;
import com.renansouza.processor.model.XML;
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
    public XMLReader processXMLReader() {
        return new XMLReader();
    }

    @Bean
    public XMLProcessor processXMLProcessor() {
        return new XMLProcessor();
    }

    @Bean
    public XMLWriter processXMLWriter() {
        return new XMLWriter();
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
    public Job processXMLJob() {
        return jobBuilderFactory.get("process-xml-job")
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .flow(step()).end().build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step").<XML, XML>chunk(chunkSize)
                .reader(processXMLReader())
                .processor(processXMLProcessor())
                .writer(processXMLWriter())
                .taskExecutor(taskExecutor())
                .listener(stepExecutionListener())
                .listener(chunkListener())
                .throttleLimit(maxThreads).build();
    }

    @Scheduled(initialDelayString = "${batch.delay:10000}", fixedDelayString = "${batch.rate:60000}")
    public void perform() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(job, params);
    }

}