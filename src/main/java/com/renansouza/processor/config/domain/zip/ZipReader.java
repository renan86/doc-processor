package com.renansouza.processor.config.domain.zip;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class ZipReader implements ItemReader<Zip> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static final Queue<Zip> zipQueue = new LinkedList<>();

//	@PostConstruct
//	public void initialize() {
//		Arrays.stream(new File(upload).listFiles()).filter(isCompressed()).forEach(file -> zipQueue.add(new Zip(file)));
//	}

	// TODO Validate if list can be updated each execution with .limit
    // TODO Do the loading when executing the class
	public synchronized Zip read() {
		Zip zip = null;

		if (zipQueue.isEmpty()) {
			return zip;
		}

		log.info("Zip Queue size {}.", zipQueue.size());
		zip = zipQueue.remove();

		return zip;
	}

}