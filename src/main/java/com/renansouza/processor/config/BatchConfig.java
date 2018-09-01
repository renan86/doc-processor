package com.renansouza.processor.config;

import com.renansouza.processor.config.xml.XmlProcessor;
import com.renansouza.processor.config.xml.XmlReader;
import com.renansouza.processor.config.xml.XmlWriter;
import com.renansouza.processor.model.XML;
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

    @Value("${com.renansouza.processor.chunk-size}")
    private int chunkSize;

    @Value("${com.renansouza.processor.max-threads}")
    private int maxThreads;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @Bean
    public XmlReader processXMLReader() {
        return new XmlReader();
    }

    @Bean
    public XmlProcessor processXMLProcessor() {
        return new XmlProcessor();
    }

    @Bean
    public XmlWriter processXMLWriter() {
        return new XmlWriter();
    }

    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(maxThreads);
        return taskExecutor;
    }

    @Bean
    public Job processXMLJob() {
        return jobBuilderFactory.get("process-xml-job")
                .incrementer(new RunIdIncrementer())
                .flow(step())
                .end()
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step").<XML, XML>chunk(chunkSize)
                .reader(processXMLReader())
                .processor(processXMLProcessor())
                .writer(processXMLWriter())
                .taskExecutor(taskExecutor())
                .throttleLimit(maxThreads).build();
    }

    @Scheduled(initialDelayString = "${batch.delay:10000}", fixedDelayString = "${batch.rate:60000}")
    public void perform() throws Exception {
        JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
        jobLauncher.run(job, params);
    }

}