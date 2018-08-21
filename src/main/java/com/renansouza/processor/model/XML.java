package com.renansouza.processor.model;

import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@ToString
public class XML {

    private final String filename;
    private final Path absolutePath;

    public XML(File file) {
        this.filename = file.getName();
        this.absolutePath = Paths.get(file.getAbsolutePath());
    }
}