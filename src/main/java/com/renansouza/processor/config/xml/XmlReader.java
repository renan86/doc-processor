package com.renansouza.processor.config.xml;

import com.renansouza.processor.model.Xml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class XmlReader implements ItemReader<Xml> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	private static final Queue<Xml> xmlQueue = new LinkedList<>();

	@PostConstruct
	public void initialize() {
		Arrays.stream(new File(upload).listFiles()).filter(file -> file.getName().endsWith("xml")).forEach(file -> xmlQueue.add(new Xml(file)));
	}

	// TODO Validate if list can be updated each execution with .limit
	public synchronized Xml read() {
		Xml xml = null;

		if (xmlQueue.isEmpty()) {
			return xml;
		}
		log.info("Xml Queue size {}.", xmlQueue.size());
		xml = xmlQueue.remove();

		return xml;
	}

}
