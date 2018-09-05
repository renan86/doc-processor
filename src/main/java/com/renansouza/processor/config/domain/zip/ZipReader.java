package com.renansouza.processor.config.domain.zip;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static com.renansouza.processor.config.domain.zip.predicates.ZipPredicates.isCompressed;

@Slf4j
public class ZipReader implements ItemReader<Zip> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static final Queue<Zip> zipQueue = new LinkedList<>();

	public void initialize() {
		Arrays.stream(new File(upload).listFiles()).filter(isCompressed()).forEach(file -> zipQueue.add(new Zip(file)));
	}

	public synchronized Zip read() {
	    initialize();
		Zip zip = null;

		if (zipQueue.isEmpty()) {
			return zip;
		}

		log.info("Zip Queue size {}.", zipQueue.size());
		zip = zipQueue.remove();

		return zip;
	}

}