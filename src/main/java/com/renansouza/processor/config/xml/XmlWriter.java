package com.renansouza.processor.config.xml;

import com.renansouza.processor.model.XML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class XmlWriter implements ItemWriter<XML> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	@Value("${com.renansouza.processor.file.upload:file/download}")
	private String download;

	@Override
	public void write(List<? extends XML> list) {

		log.info("Write xml list: {}", list);

		for (XML xml : list) {
			if (xml.isSuccess()) {
				log.info("XML was successful");
			} else {
                try {
                    Files.move(xml.getFile().toPath(), Paths.get(xml.getFile().getAbsolutePath().replace(upload, download)));
				    log.info("Moving document: {} | {}.", xml.getFile().getName(), xml.getErrors());
                } catch (IOException e) {
                    log.error("Erro while moving file {}: {} ", xml.getFile().getName(), e.getMessage());
                }
            }
		}		
	}

}
