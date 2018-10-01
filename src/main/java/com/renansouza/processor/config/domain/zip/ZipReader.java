package com.renansouza.processor.config.domain.zip;

import com.renansouza.processor.config.CommonQueues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class ZipReader implements ItemReader<Zip> {

	public synchronized Zip read() {
		Zip zip = null;

		if (CommonQueues.getZipQueue().isEmpty()) {
			return zip;
		}

		log.trace("Zip Queue size {}.", CommonQueues.getZipQueue().size());
		zip = CommonQueues.getZipQueue().remove();

		return zip;
	}

}