package com.renansouza.processor.config.zip;

import com.renansouza.processor.model.Zip;
import org.springframework.batch.item.ItemProcessor;

public class ZipProcessor implements ItemProcessor<Zip, Zip> {

	@Override
	public Zip process(Zip zip) {
		return zip;
	}

}
