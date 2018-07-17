package com.renansouza.processor.file;

import com.renansouza.processor.Constants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
@Slf4j
class FileService {

    @Value("${com.renansouza.processor.file.download:file/download}")
    private String download;

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Value("#{'${app.flow}'.split(';')}")
    private List<String> flows;

    @Value("#{'${app.environment}'.split(';')}")
    private List<String> environments;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    void save(int flow, int env, MultipartFile[] files, HttpServletRequest request) {

        Supplier<Stream<MultipartFile>> list = () -> Stream.of(files)
                                                            .filter(u -> u.getSize() != 0)
                                                            .filter(u -> StringUtils.hasText(u.getOriginalFilename()))
                                                            .filter(u -> FilenameUtils.isExtension(u.getOriginalFilename(), Constants.getAllExtensions()));

        if (list.get().count() == 0) {
            throw new IllegalArgumentException("There is no valid files to upload, please verify!");
        }
        list.get().forEach(f -> {
            val filename = environments.get(env) + ";" + flows.get(flow) + ";" + f.getOriginalFilename();

            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(upload + File.separator + filename)))) {
                stream.write(f.getBytes());
                log.info("New file saved -> Name: {} | Size: {} | Uploader: {}.", filename, FileUtils.byteCountToDisplaySize(f.getSize()), request.getRemoteAddr());
            } catch (IOException e) {
                log.error("Unable to save file {}: {}", f.getOriginalFilename(), e.getLocalizedMessage());
            }

            if (FilenameUtils.isExtension(f.getOriginalFilename(), Constants.getCompressedExtensions())) {
                try {
                    JobParameters jobParameters = new JobParametersBuilder().addString("file", filename).toJobParameters();
                    jobLauncher.run(job, jobParameters);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });
    }

    Resource retrieve(String filename) {
        try {
            Resource resource = new UrlResource(FileUtils.getFile(download + "/" + filename).toURI());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found " + filename, e);
        }
    }

}