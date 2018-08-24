package com.renansouza.processor.config.attempt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class AttemptProcessor implements ItemProcessor<Attempt,Attempt> {
	
	@Override
	public Attempt process(Attempt attempt) {
		
		log.info("Start Processing file {}.", attempt.getFile().getName());
		long processTime = System.currentTimeMillis();
//		processAttempt(attempt);
		processTime = System.currentTimeMillis() - processTime;
		log.info("{} seconds to process file {} ", ((double)processTime/1000), attempt.getFile().getName());
		
		return attempt;
	}

//	private void processAttempt(Attempt attempt) {
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			log.error("Error during sleep", e);
//		}
//	}

}
