package com.renansouza.processor.config;

import com.renansouza.processor.model.XML;
import org.springframework.batch.item.ItemWriter;

import java.nio.file.Files;
import java.util.List;

public class Writer implements ItemWriter<XML> {

    @Override
    public void write(List<? extends XML> list) {

        list.forEach(xml -> {
            try {
                System.out.println("Excluindo o arquivo em " + xml.getAbsolutePath());
                Files.deleteIfExists(xml.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Erro ao excluir arquivo em " + xml.getAbsolutePath());
            }
        });
    }
}