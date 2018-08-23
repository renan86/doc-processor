package com.renansouza.processor.config.attempt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class AttemptReader implements ItemReader<Attempt> {

	private static final Logger logger = LoggerFactory.getLogger(AttemptReader.class);

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static Queue<Attempt> attemptQueue = new LinkedList<Attempt>();

	@PostConstruct
	public void initialize() {
		logger.info("scanning file directory: {}.", upload);

		File inputDirectory = new File(upload);

		Arrays.stream(inputDirectory.listFiles()).forEach(file -> attemptQueue.add(new Attempt(file)));

		logger.info("{} attempts queued.", attemptQueue.size());
	}

	public synchronized Attempt read() {
		Attempt attempt = null;

		logger.info("Attempt Queue size {}.", attemptQueue.size());
		if (attemptQueue.size() > 0) {
			attempt = attemptQueue.remove();
		}

		return attempt;
	}

}
