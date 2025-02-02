package com.fiap.video.driver.controller;

import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.Video;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final ProcessVideoUseCase processVideoUseCase;

    public VideoController(ProcessVideoUseCase processVideoUseCase) {
        this.processVideoUseCase = processVideoUseCase;
    }

    // Processamento de vídeo usando S3
    @PostMapping("/process")
    public ResponseEntity<String> processVideo(@RequestParam String bucketName,
                                               @RequestParam String videoKey,
                                               @RequestParam int intervalSeconds
                                               ) {
        try {
            processVideoUseCase.process(bucketName, videoKey, intervalSeconds);
            return ResponseEntity.ok("Video processing completed.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during video processing: " + e.getMessage());
        }
    }

    // Obter informações do vídeo
    @GetMapping("/{bucketName}/{videoKey}")
    public ResponseEntity<Video> getVideo(@PathVariable String bucketName,
                                          @PathVariable String videoKey) {
        try {
            Video video = processVideoUseCase.getVideo(bucketName, videoKey);
            return ResponseEntity.ok(video);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}