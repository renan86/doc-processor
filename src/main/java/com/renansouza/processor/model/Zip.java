package com.renansouza.processor.model;

import com.renansouza.processor.Constants;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Zip extends Attempt {

    @Getter
    private boolean isZip;

    public Zip(File file) {
        super(file);
        this.isZip = isZip(file);
    }

    private boolean isZip(File file) {
        return FilenameUtils.isExtension(file.getName(), Constants.getCompressedExtensions());
    }

}