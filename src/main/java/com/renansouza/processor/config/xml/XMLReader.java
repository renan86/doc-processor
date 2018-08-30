package com.renansouza.processor.config.xml;

import com.renansouza.processor.model.XML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class XMLReader implements ItemReader<XML> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static final Queue<XML> xmlQueue = new LinkedList<>();

	@PostConstruct
	public void initialize() {
		log.info("scanning file directory: {}.", upload);

		File inputDirectory = new File(upload);

		Arrays.stream(inputDirectory.listFiles())
				.filter(file -> file.getName().endsWith("xml"))
				.limit(2)
				.peek(file -> log.info("Processing file {}.", file.getName()))
				.forEach(file -> xmlQueue.add(new XML(file)));

		log.info("{} xmls queued.", xmlQueue.size());
	}

	public synchronized XML read() {
		XML xml = null;

		log.info("XML Queue size {}.", xmlQueue.size());
		if (xmlQueue.size() > 0) {
			xml = xmlQueue.remove();
		}

		return xml;
	}

}
