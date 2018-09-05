package com.renansouza.processor;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    @Getter
    private static final String[] allExtensions = {"xml", "zip", "7z"};
    @Getter
    private static final String[] compressedExtensions = {"zip", "7z"};

}