package com.fiap.video.core.application.usecases;

import com.fiap.video.core.domain.Video;
import com.fiap.video.infrastructure.adapters.SNSAdapter;
import com.fiap.video.infrastructure.adapters.VideoProcessorAdapter;
import com.fiap.video.infrastructure.memory.InMemoryVideoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ProcessVideoUseCase {

    private final VideoProcessorAdapter videoProcessorAdapter;
    private final InMemoryVideoRepository videoRepository;
    private final SNSAdapter snsAdapter;

    public ProcessVideoUseCase(VideoProcessorAdapter videoProcessorAdapter, InMemoryVideoRepository videoRepository, SNSAdapter snsAdapter) {
        this.videoProcessorAdapter = videoProcessorAdapter;
        this.videoRepository = videoRepository;
        this.snsAdapter = snsAdapter;
    }

    public void process(String videoPath, String outputFolder, int intervalSeconds, String zipFilePath) {
        String idSolicitacao = UUID.randomUUID().toString();
        String usuario = "teste";
        String email = "xyz@mail.com";
        String urlVideoCortesS3 = "https://s3.amazonaws.com/meubucket/video.zip";

        try {
            snsAdapter.publishMessage(idSolicitacao, usuario, "EM_PROCESSAMENTO", email, urlVideoCortesS3);

            Video video = new Video(videoPath, Duration.ZERO);
            videoProcessorAdapter.extractFrames(video, outputFolder, intervalSeconds);
            videoProcessorAdapter.compressFrames(outputFolder, zipFilePath);
            videoRepository.save(video);

            snsAdapter.publishMessage(idSolicitacao, usuario, "PROCESSADO", email, urlVideoCortesS3);
        } catch (Exception e) {
            snsAdapter.publishMessage(idSolicitacao, usuario, "ERRO_NO_PROCESSAMENTO", email, urlVideoCortesS3);
        }
    }

    public Video getVideo(String videoPath) {
        return videoRepository.findByPath(videoPath);
    }
}