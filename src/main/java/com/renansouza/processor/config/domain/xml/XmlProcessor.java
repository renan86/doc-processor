package com.renansouza.processor.config.domain.xml;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class XmlProcessor implements ItemProcessor<Xml, Xml> {
	
	@Override
	public Xml process(Xml xml) {

		processXmlAttempt(xml);
		return xml;
	}

	private void processXmlAttempt(Xml xml) {
        if (xml.getDoc() != null && StringUtils.isNotEmpty(xml.getDoc())) {
            log.info("File valid");
        } else {
			xml.addSystemError("File invalid");
        }
    }

}
