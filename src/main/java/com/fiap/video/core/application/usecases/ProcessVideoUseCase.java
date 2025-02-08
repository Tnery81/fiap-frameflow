package com.fiap.video.core.application.usecases;

import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.domain.Video;
import com.fiap.video.core.domain.VideoMessage;
import com.fiap.video.infrastructure.adapters.VideoDownloadAdapter;
import com.fiap.video.infrastructure.adapters.SNSAdapter;
import com.fiap.video.infrastructure.adapters.VideoProcessorAdapter;
import com.fiap.video.infrastructure.exception.VideoRetrievalException;
import com.fiap.video.infrastructure.memory.InMemoryVideoRepository;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.Duration;

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

    public void process(VideoMessage videoMessage) {
        String zipFileName = videoMessage.getVideoKeyS3().replace(".mp4", ".zip");
        int intervalSeconds;

        if(videoMessage.getIntervalSeconds() != null){
            intervalSeconds = videoMessage.getIntervalSeconds();
        }else{
            intervalSeconds = 10;
        }

        try {
            snsAdapter.publishMessage(videoMessage, VideoStatus.IN_PROGRESS, zipFileName, VideoStatus.IN_PROGRESS.toString());

            File videoFile = s3Service.downloadFile(videoMessage.getVideoKeyS3());

            Video video = new Video(videoFile.getAbsolutePath(), Duration.ZERO);

            String urlZipVideoCortesS3 = videoProcessorAdapter.extractFrames(video, zipFileName, intervalSeconds);

            if (urlZipVideoCortesS3 != null) {
                videoRepository.save(video);
                snsAdapter.publishMessage(videoMessage, VideoStatus.COMPLETED, zipFileName, urlZipVideoCortesS3);
            } else {
                snsAdapter.publishMessage(videoMessage,  VideoStatus.PROCESSING_ERROR, VideoStatus.PROCESSING_ERROR.toString(),".zip");
            }


        } catch (Exception e) {
            snsAdapter.publishMessage(videoMessage,  VideoStatus.PROCESSING_ERROR, VideoStatus.PROCESSING_ERROR.toString(),".zip");
        }
    }

    public Video getVideo(String videoKey) {
        try {
            String zipKey = videoKey.replace(".mp4", ".zip");

            File zipFile = s3Service.downloadFile(zipKey);

            return new Video(zipFile.getAbsolutePath(), Duration.ZERO);
        } catch (Exception e) {
            throw new VideoRetrievalException("Erro ao obter o vídeo ZIP do S3: " + e.getMessage(), e);
        }
    }
}
