package com.renansouza.processor.config.domain.xml;

import com.renansouza.processor.config.CommonQueues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class XmlReader implements ItemReader<Xml> {

	public synchronized Xml read() {
		Xml xml = null;

		if (CommonQueues.getXmlQueue().isEmpty()) {
			return xml;
		}

		log.trace("Xml Queue size {}.", CommonQueues.getXmlQueue().size());
		xml = CommonQueues.getXmlQueue().remove();

		return xml;
	}

}
