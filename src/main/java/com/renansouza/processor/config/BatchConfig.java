package com.renansouza.processor.config;

import com.renansouza.processor.config.xml.XmlProcessor;
import com.renansouza.processor.config.xml.XmlReader;
import com.renansouza.processor.config.xml.XmlWriter;
import com.renansouza.processor.config.zip.ZipProcessor;
import com.renansouza.processor.config.zip.ZipReader;
import com.renansouza.processor.model.Xml;
import com.renansouza.processor.model.Zip;
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
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Bean
    public ZipReader zipReader() {
        return new ZipReader();
    }

    @Bean
    public ZipProcessor zipProcessor() {
        return new ZipProcessor();
    }

    @Bean
    public XmlReader xmlReader() {
        return new XmlReader();
    }

    @Bean
    public XmlProcessor xmlProcessor() {
        return new XmlProcessor();
    }

    @Bean
    public XmlWriter xmlWriter() {
        return new XmlWriter();
    }

    @Bean
    public Job processXMLJob() {
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .flow(zipStep())
                .next(xmlStep())
                .end()
                .build();
    }

    @Bean
    public Step zipStep() {
        return stepBuilderFactory.get("zipStep").<Zip, Zip>chunk(chunkSize)
                .reader(zipReader())
                .processor(zipProcessor())
                .taskExecutor(taskExecutor())
                .throttleLimit(maxThreads)
                .build();
    }

    @Bean
    public Step xmlStep() {
        return stepBuilderFactory.get("xmlStep").<Xml, Xml>chunk(chunkSize)
                .reader(xmlReader())
                .processor(xmlProcessor())
                .writer(xmlWriter())
                .taskExecutor(taskExecutor())
                .throttleLimit(maxThreads)
                .build();
    }

    @Scheduled(initialDelayString = "${batch.delay:10000}", fixedDelayString = "${batch.rate:60000}")
    public void perform() throws Exception {
        JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
        jobLauncher.run(job, params);
    }

    private TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(maxThreads);
        return taskExecutor;
    }

}