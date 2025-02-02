package com.fiap.video.core.application.usecases;

import com.fiap.video.core.domain.Video;
import com.fiap.video.infrastructure.adapters.S3Service;
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
    private final S3Service s3Service;

    public ProcessVideoUseCase(VideoProcessorAdapter videoProcessorAdapter, InMemoryVideoRepository videoRepository, SNSAdapter snsAdapter, S3Service s3Service) {
        this.videoProcessorAdapter = videoProcessorAdapter;
        this.videoRepository = videoRepository;
        this.snsAdapter = snsAdapter;
        this.s3Service = s3Service;
    }

    public void process(String bucketName, String videoKey, int intervalSeconds) {
        String idSolicitacao = UUID.randomUUID().toString();
        String usuario = "teste";
        String email = "xyz@mail.com";
        String zipFileName = idSolicitacao + ".zip";
        try {
            snsAdapter.publishMessage(idSolicitacao, usuario, "EM_PROCESSAMENTO", email, "");

            // Download do vídeo do S3
            File videoFile = s3Service.downloadFile(bucketName, videoKey);

            // Definindo o caminho do arquivo ZIP com base no nome do vídeo
            String zipFilePath = videoFile.getParent() + "/" + videoFile.getName().replace(".mp4", ".zip");

            Video video = new Video(videoFile.getAbsolutePath(), Duration.ZERO);
            videoProcessorAdapter.extractFrames(video, zipFilePath, intervalSeconds);

            String urlZipVideoCortesS3 = videoProcessorAdapter.extractFrames(video, zipFileName, intervalSeconds);

            if (urlZipVideoCortesS3 != null) {
                videoRepository.save(video);
                snsAdapter.publishMessage(idSolicitacao, usuario, "PROCESSADO", email, zipFileName);
            } else {
                snsAdapter.publishMessage(idSolicitacao, usuario, "ERRO_NO_PROCESSAMENTO", email, null);
            }



        } catch (Exception e) {
            snsAdapter.publishMessage(idSolicitacao, usuario, "ERRO_NO_PROCESSAMENTO", email, "");
        }
    }

    public Video getVideo(String bucketName, String videoKey) {
        try {
            // Alterando o nome do arquivo para o ZIP correspondente
            String zipKey = videoKey.replace(".mp4", ".zip");

            // Download do arquivo ZIP do S3
            File zipFile = s3Service.downloadFile(bucketName, zipKey);

            // Retornar o vídeo com informações básicas
            return new Video(zipFile.getAbsolutePath(), Duration.ZERO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter o vídeo ZIP do S3: " + e.getMessage(), e);
        }
    }
}
