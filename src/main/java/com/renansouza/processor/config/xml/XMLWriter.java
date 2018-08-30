package com.renansouza.processor.config.xml;

import com.renansouza.processor.model.XML;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Slf4j
public class XMLWriter implements ItemWriter<XML> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	@Value("${com.renansouza.processor.file.upload:file/download}")
	private String download;

	@Override
	public void write(List<? extends XML> xmls) {

		log.info("Write xml list: {}", xmls);

		for (XML xml : xmls) {
			if (xml.isSuccess()) {
				log.info("XML was successful");
			}
			moveFile(xml);
		}		
	}

	private void moveFile(XML xml) {
		if (!xml.hasSystemError()) {
            log.info("Deleting {} document: {} | {}.", xml.isSuccess() ? "processed" : "failed", xml.getFile().getName(), xml.getDoc());
			xml.getFile().delete();
		}
	}

}
