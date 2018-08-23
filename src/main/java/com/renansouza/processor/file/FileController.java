package com.renansouza.processor.file;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity downloadFile(@PathVariable String filename) throws IOException {

        if (StringUtils.isEmpty(filename)) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        Resource resource = fileService.retrieve(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("inline; filename=\"" + resource.getFilename() + "\"").build());
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentLength(resource.contentLength());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/countFiles")
    public ResponseEntity countFiles() {
        return new ResponseEntity("feature not implemented!", HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/upload")
    public ResponseEntity uploadFile(@RequestParam("flow") int flow, @RequestParam("env") int env, @RequestParam("file") MultipartFile[] files, HttpServletRequest request) {

        fileService.save(flow, env, files, request);
        return new ResponseEntity("Successfully uploaded files.", HttpStatus.OK);
    }

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job processJob;

    @RequestMapping("/invokejob")
    public String handle() {
        try {
            JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
            jobLauncher.run(processJob, jobParameters);

        } catch (Exception e) {
//            log.error("");
        }
        return "Batch job has been invoked";
    }

}