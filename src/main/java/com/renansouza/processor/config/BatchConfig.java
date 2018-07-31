package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class BatchConfig {

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    /**
     Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException:
     Error creating bean with name 'xmlItemReader' defined in class path resource [com/renansouza/processor/config/BatchConfig.class]:
     Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException:
     Failed to instantiate [org.springframework.batch.item.xml.StaxEventItemReader]: Factory method 'xmlItemReader' threw exception;
     nested exception is java.lang.NoClassDefFoundError: com/thoughtworks/xstream/converters/ConverterLookup
     **/

    @Bean
    public StaxEventItemReader<XML> xmlItemReader() {
        XStreamMarshaller unmarshaller = new XStreamMarshaller();

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("nfeProc", XML.class);

        unmarshaller.setAliases(aliases);

        StaxEventItemReader<XML> reader = new StaxEventItemReader<>();

        reader.setResource(new ClassPathResource(upload));
        reader.setFragmentRootElementName("nfeProc");
        reader.setUnmarshaller(unmarshaller);

        return reader;
    }

    @Bean
    public ItemWriter<XML> xmlItemWriter() {
        return items -> {
            for (XML item : items) {
                log.info(item.toString());
            }
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<XML, XML>chunk(10)
                .reader(xmlItemReader())
                .writer(xmlItemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build();
    }

}