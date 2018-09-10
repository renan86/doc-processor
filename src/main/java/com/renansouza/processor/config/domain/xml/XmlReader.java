package com.renansouza.processor.config.domain.xml;

import com.renansouza.processor.config.CommonQueues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class XmlReader implements ItemReader<Xml> {

	@Autowired
	private CommonQueues queues;

	public synchronized Xml read() {
		Xml xml = null;

		if (queues.xmlQueue.isEmpty()) {
			return xml;
		}

		log.trace("Xml Queue size {}.", CommonQueues.xmlQueue.size());
		xml = CommonQueues.xmlQueue.remove();

		return xml;
	}

}
