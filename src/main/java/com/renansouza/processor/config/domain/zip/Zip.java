package com.renansouza.processor.config.domain.zip;

import com.renansouza.processor.Constants;
import com.renansouza.processor.config.domain.Attempt;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

@Getter
public class Zip extends Attempt {

    private boolean isZip;
    private String extensioon;

    public Zip(File file) {
        super(file);
        this.isZip = isZip(file);
        this.extensioon = FilenameUtils.getExtension(file.getName());
    }

    private boolean isZip(File file) {
        return FilenameUtils.isExtension(file.getName(), Constants.getCompressedExtensions());
    }

}