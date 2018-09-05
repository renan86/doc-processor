package com.renansouza.processor.config.domain.zip;

import com.renansouza.processor.util.Decompress;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class ZipProcessor implements ItemProcessor<Zip, Zip> {

    @Autowired
    private Decompress decompress;

	@Override
	public Zip process(Zip zip) {
        decompress.unzip(zip);
		return zip;
	}

}