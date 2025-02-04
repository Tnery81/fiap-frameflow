package com.fiap.video.driver.controller;

import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.Video;
import com.fiap.video.core.domain.VideoMessage;
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
    public String processVideo(@RequestBody VideoMessage videoMessage) {
        try {
            processVideoUseCase.process(
                    videoMessage
            );
            return "Processamento iniciado com sucesso!";
        } catch (Exception e) {
            return "Erro ao iniciar o processamento: " + e.getMessage();
        }
    }

    @GetMapping("/{videoKey}")
    public ResponseEntity<Video> getVideo(
            @PathVariable String videoKey) {
        try {
            Video video = processVideoUseCase.getVideo(videoKey);
            return ResponseEntity.ok(video);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}