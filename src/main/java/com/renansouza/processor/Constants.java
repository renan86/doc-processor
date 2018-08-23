package com.renansouza.processor;

public class Constants {

    private static final String[] allExtensions = {"xml", "zip", "7z"};
    private static final String[] compressedExtensions = {"zip", "7z"};

    public Constants() {
    }

    public static String[] getAllExtensions() {
        return allExtensions;
    }

    public static String[] getCompressedExtensions() {
        return compressedExtensions;
    }
}