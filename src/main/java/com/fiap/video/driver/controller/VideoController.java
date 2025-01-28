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

    @PostMapping("/process")
    public ResponseEntity<String> processVideo(@RequestParam String videoPath,
                                                @RequestParam String outputFolder,
                                                @RequestParam int intervalSeconds,
                                                @RequestParam String zipFilePath) {
        processVideoUseCase.process(videoPath, outputFolder, intervalSeconds, zipFilePath);
        return ResponseEntity.ok("Video processing completed.");
    }

    @GetMapping("/{videoPath}")
    public ResponseEntity<Video> getVideo(@PathVariable String videoPath) {
        Video video = processVideoUseCase.getVideo(videoPath);
        return ResponseEntity.ok(video);
    }
}
