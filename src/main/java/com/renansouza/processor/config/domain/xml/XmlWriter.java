package com.renansouza.processor.config.domain.xml;

import com.renansouza.processor.config.CommonQueues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class XmlWriter implements ItemWriter<Xml> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	@Value("${com.renansouza.processor.file.upload:file/download}")
	private String download;

	@Override
	public void write(List<? extends Xml> list) {

		log.info("Write xml list: {}", list);

		for (Xml xml : list) {
			if (xml.isSuccess()) {
				try {
					Files.deleteIfExists(xml.getFile().toPath());
				} catch (IOException e) {
					log.error("Error while deleting file {}: {} ", xml.getFile().getName(), e.getMessage());
				}
			} else {
                try {
                    Files.move(xml.getFile().toPath(), Paths.get(xml.getFile().getAbsolutePath().replace(upload, download)));
				    log.info("Moving document: {} | {}.", xml.getFile().getName(), xml.getErrors());
                } catch (IOException e) {
                    log.error("Error while moving file {}: {} ", xml.getFile().getName(), e.getMessage());
                }
            }
			CommonQueues.getXmlQueue().remove(xml);
		}		
	}

}
