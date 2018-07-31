package com.renansouza.processor.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class XML {

    private final String filename;
    private final String accesskey;

    public XML(String filename, String accesskey) {
        this.filename = filename;
        this.accesskey = accesskey;
    }
}