package com.renansouza.processor.config.domain.zip;

import com.renansouza.processor.config.CommonQueues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ZipReader implements ItemReader<Zip> {

	@Autowired
	private CommonQueues queues;

	public synchronized Zip read() {
		Zip zip = null;

		if (CommonQueues.zipQueue.isEmpty()) {
			return zip;
		}

		log.trace("Zip Queue size {}.", CommonQueues.zipQueue.size());
		zip = CommonQueues.zipQueue.remove();

		return zip;
	}

}