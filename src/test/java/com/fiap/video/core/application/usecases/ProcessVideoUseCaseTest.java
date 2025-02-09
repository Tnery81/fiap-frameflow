package com.fiap.video.core.application.usecases;

import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.domain.Video;
import com.fiap.video.core.domain.VideoMessage;
import com.fiap.video.infrastructure.adapters.VideoDownloadAdapter;
import com.fiap.video.infrastructure.adapters.SNSAdapter;
import com.fiap.video.infrastructure.adapters.VideoProcessorAdapter;
import com.fiap.video.infrastructure.memory.InMemoryVideoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.File;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProcessVideoUseCaseTest {

    @InjectMocks
    private ProcessVideoUseCase processVideoUseCase;

    @Mock
    private VideoProcessorAdapter videoProcessorAdapter;

    @Mock
    private InMemoryVideoRepository videoRepository;

    @Mock
    private SNSAdapter snsAdapter;

    @Mock
    private VideoDownloadAdapter s3Service;

    @Mock
    private VideoMessage videoMessage;

    @Mock
    private File mockFile;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testProcessVideoSuccess() {
        String videoKey = "video.mp4";
        String zipFileName = videoKey.replace(".mp4", ".zip");
        String expectedUrl = "mock-url-to-zip";
        int intervalSeconds = 10;

        when(videoMessage.getVideoKeyS3()).thenReturn(videoKey);
        when(videoMessage.getIntervalSeconds()).thenReturn(intervalSeconds);
        when(s3Service.downloadFile(videoKey)).thenReturn(mockFile);
        when(videoProcessorAdapter.extractFrames(any(), eq(zipFileName), eq(intervalSeconds))).thenReturn(expectedUrl);

        processVideoUseCase.process(videoMessage);

        verify(snsAdapter, times(1)).publishMessage(videoMessage, VideoStatus.IN_PROGRESS, zipFileName, VideoStatus.IN_PROGRESS.toString());
        verify(snsAdapter, times(1)).publishMessage(videoMessage, VideoStatus.COMPLETED, zipFileName, expectedUrl);
        verify(videoRepository, times(1)).save(any(Video.class));
    }


    @Test
    void testProcessVideoFailure() {
        String videoKey = "video.mp4";
        String zipFileName = videoKey.replace(".mp4", ".zip");

        when(videoMessage.getVideoKeyS3()).thenReturn(videoKey);
        when(s3Service.downloadFile(videoKey)).thenReturn(mockFile);

        processVideoUseCase.process(videoMessage);

        verify(snsAdapter, times(1)).publishMessage(videoMessage, VideoStatus.IN_PROGRESS, zipFileName, VideoStatus.IN_PROGRESS.toString());
        verify(snsAdapter, times(1)).publishMessage(videoMessage, VideoStatus.PROCESSING_ERROR, VideoStatus.PROCESSING_ERROR.toString(), ".zip");
        verify(videoRepository, times(0)).save(any());
    }

    @Test
    void testProcessVideoExceptionHandling() {
        String videoKey = "video.mp4";
        String zipFileName = videoKey.replace(".mp4", ".zip");

        when(videoMessage.getVideoKeyS3()).thenReturn(videoKey);
        when(s3Service.downloadFile(videoKey)).thenThrow(new RuntimeException("Download failed"));

        processVideoUseCase.process(videoMessage);

        verify(snsAdapter, times(1)).publishMessage(videoMessage, VideoStatus.IN_PROGRESS, zipFileName, VideoStatus.IN_PROGRESS.toString());
        verify(snsAdapter, times(1)).publishMessage(videoMessage, VideoStatus.PROCESSING_ERROR, VideoStatus.PROCESSING_ERROR.toString(), ".zip");
    }


    @Test
    void testGetVideoSuccess() {
        String videoKey = "video.mp4";
        String zipKey = videoKey.replace(".mp4", ".zip");
        when(s3Service.downloadFile(zipKey)).thenReturn(mockFile);

        Video video = processVideoUseCase.getVideo(videoKey);

        assertNotNull(video, "Video should be returned");
        verify(s3Service, times(1)).downloadFile(zipKey);
    }

    @Test
    void testGetVideoFailure() {
        String videoKey = "video.mp4";
        String zipKey = videoKey.replace(".mp4", ".zip");
        when(s3Service.downloadFile(zipKey)).thenThrow(new RuntimeException("Download failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> processVideoUseCase.getVideo(videoKey));
        assertEquals("Erro ao obter o vídeo ZIP do S3: Download failed", exception.getMessage());
    }
}
