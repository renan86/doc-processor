package com.renansouza.processor.config.xml;

import com.renansouza.processor.model.XML;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class XMLProcessor implements ItemProcessor<XML, XML> {
	
	@Override
	public XML process(XML xml) {
		
		log.info("Start Processing file {}.", xml.getFile().getName());
		long processTime = System.currentTimeMillis();
		processAttempt(xml);
		processTime = System.currentTimeMillis() - processTime;
		log.info("{} seconds to process file {} ", ((double)processTime/1000), xml.getFile().getName());
		
		return xml;
	}

	private void processAttempt(XML xml) {
        if (xml.getDoc() != null && StringUtils.isNotEmpty(xml.getDoc())) {
            log.info("Arquivo válido");
        } else {
			xml.addSystemError("Arquivo inválido");
        }
    }

}
