package com.renansouza.processor.config;

import com.renansouza.processor.config.domain.xml.Xml;
import com.renansouza.processor.config.domain.xml.XmlProcessor;
import com.renansouza.processor.config.domain.xml.XmlReader;
import com.renansouza.processor.config.domain.xml.XmlWriter;
import com.renansouza.processor.config.domain.zip.Zip;
import com.renansouza.processor.config.domain.zip.ZipProcessor;
import com.renansouza.processor.config.domain.zip.ZipReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowStep;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Slf4j
class BatchConfig {

    // https://examples.javacodegeeks.com/enterprise-java/spring/batch/spring-batch-parallel-processing-example/
    // https://stackoverflow.com/questions/38949030/spring-batch-execute-dynamically-generated-steps-in-a-tasklet

    //TODO rename createDynamicStepsTasklet to xmlStepTasklet
    //TODO clone xmlStepTasklet as model to zipStepTasklet
    //TODO cleanup commented lines
    //TODO test if code works

    @Value("${com.renansouza.processor.chunk-size}")
    private int chunkSize;

    @Value("${com.renansouza.processor.max-threads}")
    private int maxThreads;

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Value("#{'${com.renansouza.processor.environment}'.split(';')}")
    private List<String> environments;

    private List<Step> dynamicSteps = Collections.synchronizedList(new ArrayList<Step>()) ;

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
    public Job job(@Qualifier("createDynamicStepsStep") Step createDynamicStepsStep,
                   @Qualifier("executeDynamicStepsStep") Step executeDynamicStepsStep) {
        return jobBuilderFactory.get("job")
                /*.start(startStep())
                .next(zipStep())
                .next(xmlStep())*/
                .start(createDynamicStepsStep)
                .next(executeDynamicStepsStep)
                .build();
    }

    public Flow createParallelFlow() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(1);

        List<Flow> flows = dynamicSteps.stream()
                .map(step -> new FlowBuilder<Flow>("flow_" + step.getName()).start(step).build())
                .collect(Collectors.toList());

        return new FlowBuilder<SimpleFlow>("parallelStepsFlow")
                .split(taskExecutor)
                .add(flows.toArray(new Flow[flows.size()]))
                .build();
    }

    @Bean
    public Step createDynamicStepsStep(@Qualifier("createDynamicStepsTasklet") Tasklet createDynamicStepsTasklet) {
        return  stepBuilderFactory
                .get("createDynamicStepsStep")
                .tasklet(createDynamicStepsTasklet)
                .build();
    }

    @Bean
    public Step executeDynamicStepsStep(@Qualifier("executeDynamicStepsTasklet")  Tasklet executeDynamicStepsTasklet) {
        return  stepBuilderFactory
                .get("executeDynamicStepsStep")
                .tasklet(executeDynamicStepsTasklet)
                .build();
    }

    @Bean
    public Tasklet executeDynamicStepsTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                FlowStep flowStep = new FlowStep(createParallelFlow());
                SimpleJobBuilder jobBuilder = jobBuilderFactory.get("myNewJob").start(flowStep);
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    @JobScope
    public Tasklet createDynamicStepsTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                for (String environment : environments) {
                    Step dynamicStep = stepBuilderFactory.get(environment)
                                        .<Xml,Xml> chunk(chunkSize)
                                        .reader(xmlReader())
                                        .processor(xmlProcessor())
                                        .writer(xmlWriter())
                                        .taskExecutor(taskExecutor())
                                        .throttleLimit(maxThreads)
                                        .build();

                    dynamicSteps.add(dynamicStep);
                }
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Scheduled(initialDelayString = "${batch.delay:10000}", fixedDelayString = "${batch.rate:30000}")
    public void perform() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
        jobLauncher.run(job, params);
    }

    private TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(maxThreads);
        return taskExecutor;
    }

}