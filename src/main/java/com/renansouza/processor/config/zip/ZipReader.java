package com.renansouza.processor.config.zip;

import com.renansouza.processor.Constants;
import com.renansouza.processor.model.Zip;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class ZipReader implements ItemReader<Zip> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static final Queue<Zip> zipQueue = new LinkedList<>();

	@PostConstruct
	public void initialize() {
		Arrays.stream(new File(upload).listFiles())
				.filter(file -> FilenameUtils.isExtension(file.getName(), Constants.getCompressedExtensions()))
				.forEach(file -> zipQueue.add(new Zip(file)));
	}

	// TODO Validate if list can be updated each execution with .limit
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
