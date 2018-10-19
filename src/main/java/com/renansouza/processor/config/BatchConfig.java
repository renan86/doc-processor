package com.renansouza.processor.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.MalformedURLException;
import java.util.*;


@Configuration
@EnableBatchProcessing
@EnableScheduling
class BatchConfig {

    @Value("${com.renansouza.processor.chunk-size}")
    private int chunkSize;

    @Value("${com.renansouza.processor.max-threads}")
    private int maxThreads;

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Value("#{'${com.renansouza.processor.environment}'.split(';')}")
    private Set<String> environments;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobLauncher jobLauncher;

//    @Autowired
//    private Job job;
//
//    @Bean
//    public ZipReader zipReader() {
//        return new ZipReader();
//    }
//
//    @Bean
//    public ZipProcessor zipProcessor() {
//        return new ZipProcessor();
//    }
//
//    @Bean
//    public XmlReader xmlReader() {
//        return new XmlReader();
//    }
//
//    @Bean
//    public XmlProcessor xmlProcessor() {
//        return new XmlProcessor();
//    }
//
//    @Bean
//    public XmlWriter xmlWriter() {
//        return new XmlWriter();
//    }
//
//    @Bean
//    public Step startStep() {
//        return stepBuilderFactory.get("startStep").tasklet((contribution, chunkContext) -> {
//            CommonQueues.getAttemptQueue().clear();
//            Arrays.stream(Objects.requireNonNull(new File(upload).listFiles())).map(Attempt::new).filter(Attempt::isAttemptable).forEach(attempt -> /*CommonQueues.attemptQueue.add(attempt)*/ {
//                if (FilenameUtils.isExtension(attempt.getFile().getName(), Constants.getCompressedExtensions())) {
//                    CommonQueues.getZipQueue().add(new Zip(attempt.getFile()));
//                } else {
//                    CommonQueues.getXmlQueue().add(new Xml(attempt.getFile()));
//                }
//            });
//            return RepeatStatus.FINISHED;
//        }).build();
//    }
//
//    @Bean
//    public Step zipStep() {
//        return stepBuilderFactory.get("zipStep").<Zip, Zip>chunk(chunkSize)
//                .reader(zipReader())
//                .processor(zipProcessor())
//                .taskExecutor(taskExecutor())
//                .throttleLimit(maxThreads)
//                .build();
//    }
//
//    @Bean
//    public Step xmlStep() {
//        return stepBuilderFactory.get("xmlStep").<Xml, Xml>chunk(chunkSize)
//                .reader(xmlReader())
//                .processor(xmlProcessor())
//                .writer(xmlWriter())
//                .taskExecutor(taskExecutor())
//                .throttleLimit(maxThreads)
//                .build();
//    }
//
//    @Bean
//    public Job job() {
//        return jobBuilderFactory.get("job")
//                .start(startStep())
//                .next(zipStep())
//                .next(xmlStep())
//                .build();
//    }
//
    @Scheduled(initialDelayString = "${batch.delay:10000}", fixedDelayString = "${batch.rate:30000}")
    public void perform() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
        jobLauncher.run(parallelStepsJob(), params);
    }

    private TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(maxThreads);
        return taskExecutor;
    }

    @Bean
    public Job parallelStepsJob() {

        //https://examples.javacodegeeks.com/enterprise-java/spring/batch/spring-batch-parallel-processing-example/
        //https://stackoverflow.com/questions/38949030/spring-batch-execute-dynamically-generated-steps-in-a-tasklet
        //https://stackoverflow.com/questions/37310658/spring-batch-how-to-generate-parallel-steps-based-on-params-created-in-a-previ
        //https://stackoverflow.com/questions/37238813/spring-batch-looping-a-reader-processor-writer-step
        Flow masterFlow = new FlowBuilder<Flow>("masterFlow").start(taskletStep("step1")).build();


        Flow flowJob1 = new FlowBuilder<Flow>("flow1").start(taskletStep("step2")).build();
        Flow flowJob2 = new FlowBuilder<Flow>("flow2").start(taskletStep("step3")).build();
        Flow flowJob3 = new FlowBuilder<Flow>("flow3").start(taskletStep("step4")).build();

        Flow slaveFlow = new FlowBuilder<Flow>("splitflow").split(new SimpleAsyncTaskExecutor()).add(flowJob1, flowJob2, flowJob3).build();


        return (jobBuilderFactory.get("parallelFlowJob")
                .incrementer(new RunIdIncrementer())
                .start(masterFlow)
                .next(slaveFlow)
                .build()).build();
    }

    private TaskletStep taskletStep(String step) {
        return stepBuilderFactory.get(step).tasklet((contribution, chunkContext) -> {
            System.out.println(step);
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Job partitioningJob() throws Exception {
        return jobBuilderFactory.get("parallelJob")
                .incrementer(new RunIdIncrementer())
                .flow(masterStep())
                .end()
                .build();
    }

    @Bean
    public Step masterStep() throws Exception {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep())
                .gridSize(10)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step slaveStep() throws Exception {
        return stepBuilderFactory.get("slaveStep")
                .<Map<String, String>, Map<String, String>>chunk(1)
                .reader(reader(null))
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Map<String, String>> reader(@Value("#{stepExecutionContext['fileName']}") String file) throws MalformedURLException {
        FlatFileItemReader<Map<String, String>> reader = new FlatFileItemReader<>();
        reader.setResource(new UrlResource(file));

        DefaultLineMapper<Map<String, String>> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(":");
        tokenizer.setNames("key", "value");

        lineMapper.setFieldSetMapper((fieldSet) -> {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(fieldSet.readString("key"), fieldSet.readString("value"));
            return map;
        });
        lineMapper.setLineTokenizer(tokenizer);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    public ItemWriter<Map<String, String>> writer() {
        return (items) -> items.forEach(item -> {
            item.entrySet().forEach(entry -> {
                System.out.println("key->[" + entry.getKey() + "] Value ->[" + entry.getValue() + "]");
            });
        });
    }

}