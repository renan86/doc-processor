package com.renansouza.processor.model;

import com.renansouza.processor.Constants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Component
@Slf4j
@Scope("prototype")
public class ZIP {

    public void unzip(File file) throws IOException {

        switch (FilenameUtils.getExtension(file.toString())) {
            case "7z":
                decompress7Zip(file);
                break;
            case "zip":
                decompressZip(file);
                break;
            default: throw new IllegalArgumentException("Extension not allowed, verify!");
        }
    }

    private void decompress7Zip(File file) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (isNotValid(entry)) {
                    log.trace("Skipping {} {} from decompress.", (entry.isDirectory() ? "folder" : "file"), entry.getName());
                    continue;
                }

                val newFile = newFileFromEntry(file, entry);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                    log.debug("File {} decompressed successfully.", entry.getName());
                }

                if (FilenameUtils.isExtension(entry.getName(), Constants.getCompressedExtensions())) {
                    decompress7Zip(newFile);
                }
            }
        }
        Files.delete(file.toPath());
        log.debug("File {} deleted successfully", file.getName());
    }

    private void decompressZip(File file) throws IOException {
        try (ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(new FileInputStream(file))) {
            ZipArchiveEntry entry;
            while ((entry = zipArchiveInputStream.getNextZipEntry()) != null) {
                if (isNotValid(entry)) {
                    log.trace("Skipping {} {} from decompress.", (entry.isDirectory() ? "folder" : "file"), entry.getName());
                    continue;
                }

                val newFile = newFileFromEntry(file, entry);
                try (FileOutputStream output = new FileOutputStream(newFile)) {
                    IOUtils.copy(zipArchiveInputStream, output);
                    log.debug("File {} decompressed successfully.", entry.getName());
                }

                if (FilenameUtils.isExtension(entry.getName(), Constants.getCompressedExtensions())) {
                    decompressZip(newFile);
                }
            }
        }
        Files.delete(file.toPath());
        log.debug("File {} deleted successfully", file.getName());
    }

    private boolean isNotValid(ArchiveEntry entry) {
        return entry.isDirectory() || !FilenameUtils.isExtension(entry.getName(), Constants.getAllExtensions());
    }

    private File newFileFromEntry(File file, ArchiveEntry entry) {
        String filename = file.getParent();
        filename += File.separator;
        filename += StringUtils.substringBeforeLast(file.getName(), ";");
        filename += ";";

        if (entry.getName().contains("/")) {
            filename += StringUtils.substringAfterLast(entry.getName(), "/");
        } else {
            filename += entry.getName();
        }

        return new File(filename);

    }

}