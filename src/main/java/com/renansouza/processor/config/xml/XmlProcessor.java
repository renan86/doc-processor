package com.renansouza.processor.config.xml;

import com.renansouza.processor.model.XML;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class XmlProcessor implements ItemProcessor<XML, XML> {
	
	@Override
	public XML process(XML xml) {

		processAttempt(xml);
		return xml;
	}

	private void processAttempt(XML xml) {
        if (xml.getDoc() != null && StringUtils.isNotEmpty(xml.getDoc())) {
            log.info("File valid");
        } else {
			xml.addSystemError("File invalid");
        }
    }

}
