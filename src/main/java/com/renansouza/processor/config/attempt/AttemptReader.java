package com.renansouza.processor.config.attempt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class AttemptReader implements ItemReader<Attempt> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static final Queue<Attempt> attemptQueue = new LinkedList<>();

	@PostConstruct
	public void initialize() {
		log.info("scanning file directory: {}.", upload);

		File inputDirectory = new File(upload);

		Arrays.stream(inputDirectory.listFiles())
				.limit(2)
				.peek(System.out::println)
				.forEach(file -> attemptQueue.add(new Attempt(file)));

		log.info("{} attempts queued.", attemptQueue.size());
	}

	public synchronized Attempt read() {
		Attempt attempt = null;

		log.info("Attempt Queue size {}.", attemptQueue.size());
		if (attemptQueue.size() > 0) {
			attempt = attemptQueue.remove();
		}

		return attempt;
	}

}
