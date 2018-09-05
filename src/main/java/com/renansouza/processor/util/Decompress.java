package com.renansouza.processor.util;

import com.renansouza.processor.Constants;
import com.renansouza.processor.config.domain.zip.Zip;
import lombok.extern.slf4j.Slf4j;
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

import java.io.*;
import java.nio.file.Files;

@Component
@Scope("prototype")
@Slf4j
public class Decompress {

    public void unzip(Zip zip) {

        switch (zip.getExtensioon()) {
            case "7z":
                decompress7Zip(zip.getFile());
                break;
            case "zip":
                decompressZip(zip.getFile());
                break;
            default: throw new IllegalArgumentException("Extension not allowed, verify!");
        }
    }

    private void decompress7Zip(File file) {
        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (isNotValid(entry)) {
                    log.trace("Skipping {} {} from decompress.", (entry.isDirectory() ? "folder" : "file"), entry.getName());
                    continue;
                }

                var newFile = newFileFromEntry(file, entry);
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
        } catch (IOException e) {
            log.error("Error while decompressing {}.", e.getLocalizedMessage());
        }
        delete(file);
    }

    private void decompressZip(File file) {
        try (ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(new FileInputStream(file))) {
            ZipArchiveEntry entry;
            while ((entry = zipArchiveInputStream.getNextZipEntry()) != null) {
                if (isNotValid(entry)) {
                    log.trace("Skipping {} {} from decompress.", (entry.isDirectory() ? "folder" : "file"), entry.getName());
                    continue;
                }

                var newFile = newFileFromEntry(file, entry);
                try (FileOutputStream output = new FileOutputStream(newFile)) {
                    IOUtils.copy(zipArchiveInputStream, output);
                    log.debug("File {} decompressed successfully.", entry.getName());
                }

                if (FilenameUtils.isExtension(entry.getName(), Constants.getCompressedExtensions())) {
                    decompressZip(newFile);
                }
            }
        } catch (IOException e) {
            log.error("Error while decompressing {}.", e.getLocalizedMessage());
        }
        delete(file);
    }

    private boolean isNotValid(ArchiveEntry entry) {
        return entry.isDirectory() || !FilenameUtils.isExtension(entry.getName(), Constants.getAllExtensions());
    }

    private static void delete(File file){
        try {
            Files.delete(file.toPath());
            log.debug("File {} deleted successfully", file.getName());
        } catch (IOException e) {
            log.error("Error while deleting {}.", e.getLocalizedMessage());
        }
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