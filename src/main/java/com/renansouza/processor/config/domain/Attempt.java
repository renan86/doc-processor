package com.renansouza.processor.config.domain;

import com.renansouza.processor.Constants;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Getter
public class Attempt {

	private final File file;
	private boolean success;
	private boolean systemError;
	private final List<String> errors;
	private final boolean isAttemptable;
	
	public Attempt(File file) {
		this.file = file;
		this.success = true;
		this.systemError = false;
		this.errors = new LinkedList<>();
		this.isAttemptable = FilenameUtils.isExtension(file.getName(), Constants.getAllExtensions());
	}

	private void addError(String error) {
		this.success = false;
		errors.add(error);
	}

	public void addSystemError(String error) {
		addError(error);
		this.systemError = true;
	}

	@Override
	public String toString() {
		return "Attempt[" + file.getName() + ", " + success + "]";
	}

}