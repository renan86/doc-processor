package com.renansouza.processor.config.domain.zip.predicates;

import com.renansouza.processor.Constants;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.function.Predicate;

@UtilityClass
public class ZipPredicates {

    public static Predicate<File> isCompressed() {
        return file -> FilenameUtils.isExtension(file.getName(), Constants.getCompressedExtensions());
    }
}
