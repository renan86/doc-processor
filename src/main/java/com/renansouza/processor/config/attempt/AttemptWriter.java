package com.renansouza.processor.config.attempt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class AttemptWriter implements ItemWriter<Attempt> {

	@Value("${com.renansouza.processor.file.upload:file/upload}")
	private String upload;

	@Value("${com.renansouza.processor.file.upload:file/download}")
	private String download;

	@Override
	public void write(List<? extends Attempt> attempts) {

		log.info("Write attempt list: {}",attempts);

		for (Attempt attempt : attempts) {
			if (attempt.isSuccess()) {
				log.info("Attempt was successful");
			}
			moveFile(attempt);
		}		
	}

	private void moveFile(Attempt attempt) {
		if (!attempt.hasSystemError()) {
			File processedDateDirectory = new File(upload + System.getProperty("file.separator")
					+ new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));
			File failedDateDirectory = new File(download + System.getProperty("file.separator")
					+ new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));

			log.info("Moving {} document: {}.", attempt.isSuccess() ? "processed" : "failed",
					attempt.getFile().getName());

			String directory = attempt.isSuccess() ? processedDateDirectory.getAbsolutePath()
					: failedDateDirectory.getAbsolutePath();

			attempt.getFile()
					.renameTo(new File(directory + System.getProperty("file.separator") + attempt.getFile().getName()));
		}
	}

}
