package com.fiap.video.core.application.usecases;

import com.fiap.video.core.domain.Video;
import com.fiap.video.infrastructure.adapters.VideoProcessorAdapter;
import com.fiap.video.infrastructure.memory.InMemoryVideoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ProcessVideoUseCase {

    private final VideoProcessorAdapter videoProcessorAdapter;
    private final InMemoryVideoRepository videoRepository;

    public ProcessVideoUseCase(VideoProcessorAdapter videoProcessorAdapter, InMemoryVideoRepository videoRepository) {
        this.videoProcessorAdapter = videoProcessorAdapter;
        this.videoRepository = videoRepository;
    }

    public void process(String videoPath, String outputFolder, int intervalSeconds, String zipFilePath) {
        Video video = new Video(videoPath, Duration.ZERO); // Placeholder for duration calculation
        videoProcessorAdapter.extractFrames(video, outputFolder, intervalSeconds);
        videoProcessorAdapter.compressFrames(outputFolder, zipFilePath);
        videoRepository.save(video);
    }

    public Video getVideo(String videoPath) {
        return videoRepository.findByPath(videoPath);
    }
}
