package com.fiap.video.core.application.usecases;

import com.fiap.video.core.domain.Video;
import com.fiap.video.infrastructure.adapters.VideoDownloadAdapter;
import com.fiap.video.infrastructure.adapters.SNSAdapter;
import com.fiap.video.infrastructure.adapters.VideoProcessorAdapter;
import com.fiap.video.infrastructure.memory.InMemoryVideoRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Service
public class ProcessVideoUseCase {

    private final VideoProcessorAdapter videoProcessorAdapter;
    private final InMemoryVideoRepository videoRepository;
    private final SNSAdapter snsAdapter;
    private final VideoDownloadAdapter s3Service;

    public ProcessVideoUseCase(VideoProcessorAdapter videoProcessorAdapter, InMemoryVideoRepository videoRepository, SNSAdapter snsAdapter, VideoDownloadAdapter s3Service) {
        this.videoProcessorAdapter = videoProcessorAdapter;
        this.videoRepository = videoRepository;
        this.snsAdapter = snsAdapter;
        this.s3Service = s3Service;
    }

    public void process(String videoKey, int intervalSeconds) {
        String idSolicitacao = UUID.randomUUID().toString();
        String usuario = "teste";
        String email = "xyz@mail.com";
        String zipFileName = idSolicitacao + ".zip";

        try {
            snsAdapter.publishMessage(idSolicitacao, usuario, "EM_PROCESSAMENTO", email, zipFileName);

            File videoFile = s3Service.downloadFile(videoKey);

            Video video = new Video(videoFile.getAbsolutePath(), Duration.ZERO);

            String urlZipVideoCortesS3 = videoProcessorAdapter.extractFrames(video, zipFileName, intervalSeconds);

            if (urlZipVideoCortesS3 != null) {
                videoRepository.save(video);
                snsAdapter.publishMessage(idSolicitacao, usuario, "PROCESSADO", email, urlZipVideoCortesS3);
            } else {
                snsAdapter.publishMessage(idSolicitacao, usuario, "ERRO_NO_PROCESSAMENTO", email, ".zip");
            }


        } catch (Exception e) {
            snsAdapter.publishMessage(idSolicitacao, usuario, "ERRO_NO_PROCESSAMENTO", email, ".zip");
        }
    }

    public Video getVideo(String videoKey) {
        try {
            String zipKey = videoKey.replace(".mp4", ".zip");

            File zipFile = s3Service.downloadFile(zipKey);

            return new Video(zipFile.getAbsolutePath(), Duration.ZERO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter o vídeo ZIP do S3: " + e.getMessage(), e);
        }
    }
}
