package com.renansouza.processor.config;

import com.renansouza.processor.Constants;
import com.renansouza.processor.config.domain.Attempt;
import com.renansouza.processor.config.domain.xml.Xml;
import com.renansouza.processor.config.domain.xml.XmlProcessor;
import com.renansouza.processor.config.domain.xml.XmlReader;
import com.renansouza.processor.config.domain.xml.XmlWriter;
import com.renansouza.processor.config.domain.zip.Zip;
import com.renansouza.processor.config.domain.zip.ZipProcessor;
import com.renansouza.processor.config.domain.zip.ZipReader;
import org.apache.commons.io.FilenameUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.util.Arrays;


@Configuration
@EnableScheduling
class BatchConfig {

    @Value("${com.renansouza.processor.chunk-size}")
    private int chunkSize;

    @Value("${com.renansouza.processor.max-threads}")
    private int maxThreads;

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

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
    public Step startStep() {
        return stepBuilderFactory.get("startStep")
            .tasklet((contribution, chunkContext) -> {
                CommonQueues.attemptQueue.clear();
                Arrays.stream(new File(upload).listFiles()).map(Attempt::new).filter(Attempt::isAttemptable).forEach(attempt -> /*CommonQueues.attemptQueue.add(attempt)*/ {
                    if (FilenameUtils.isExtension(attempt.getFile().getName(), Constants.getCompressedExtensions())) {
                        CommonQueues.zipQueue.add(new Zip(attempt.getFile()));
                    } else {
                        CommonQueues.xmlQueue.add(new Xml(attempt.getFile()));
                    }
                });
                return RepeatStatus.FINISHED;
            }).build();
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

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(startStep())
                .next(zipStep())
                .next(xmlStep())
                .build();
    }

    @Scheduled(initialDelayString = "${batch.delay:10000}", fixedDelayString = "${batch.rate:30000}")
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